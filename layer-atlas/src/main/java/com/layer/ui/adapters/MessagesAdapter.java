package com.layer.ui.adapters;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.ListViewController;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.RecyclerViewController;
import com.layer.ui.R;
import com.layer.ui.avatar.AvatarView;
import com.layer.ui.avatar.AvatarViewModelImpl;
import com.layer.ui.avatar.IdentityNameFormatterImpl;
import com.layer.ui.databinding.UiMessageItemMeBinding;
import com.layer.ui.databinding.UiMessageItemThemBinding;
import com.layer.ui.messageitem.MessageItemViewModel;
import com.layer.ui.messagetypes.CellFactory;
import com.layer.ui.messagetypes.MessageStyle;
import com.layer.ui.presence.PresenceView;
import com.layer.ui.util.IdentityRecyclerViewEventListener;
import com.layer.ui.util.Log;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * MessagesAdapter drives an AtlasMessagesList.  The MessagesAdapter itself handles
 * rendering sender names, avatars, dates, left/right alignment, and message clustering, and leaves
 * rendering message content up to registered CellFactories.  Each CellFactory knows which Messages
 * it can render, can create new View hierarchies for its Message types, and can render (bind)
 * Message data with its created View hierarchies.  Typically, CellFactories are segregated by
 * MessagePart MIME types (e.g. "text/plain", "image/jpeg", and "application/vnd.geo+json").
 * <p>
 * Under the hood, the MessagesAdapter is a RecyclerView.Adapter, which automatically recycles
 * its list items within view-type "buckets".  Each registered CellFactory actually creates two such
 * view-types: one for cells sent by the authenticated user, and another for cells sent by remote
 * actors.  This allows the MessagesAdapter to efficiently render images sent by the current
 * user aligned on the left, and images sent by others aligned on the right, for example.  In case
 * this sent-by distinction is of value when rendering cells, it provided as the `isMe` argument.
 * <p>
 * When rendering Messages, the MessagesAdapter first determines which CellFactory to handle
 * the Message with calling CellFactory.isBindable() on each of its registered CellFactories. The
 * first CellFactory to return `true` is used for that Message.  Then, the adapter checks for
 * available CellHolders of that type.  If none are found, a new one is created with a call to
 * CellFactory.createCellHolder().  After creating a new CellHolder (or reusing an available one),
 * the CellHolder is rendered in the UI with Message data via CellFactory.bindCellHolder().
 *
 * @see CellFactory
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> implements
        BaseAdapter<Message>, RecyclerViewController.Callback {
    private final static int VIEW_TYPE_FOOTER = 0;
    protected final LayerClient mLayerClient;
    private final RecyclerViewController<Message> mQueryController;
    protected final LayoutInflater mLayoutInflater;
    protected final Handler mUiThreadHandler;
    protected OnMessageAppendListener mAppendListener;
    protected final DisplayMetrics mDisplayMetrics;
    private final IdentityRecyclerViewEventListener mIdentityEventListener;

    // Cells
    protected int mViewTypeCount = VIEW_TYPE_FOOTER;
    protected final Set<CellFactory> mCellFactories = new LinkedHashSet<CellFactory>();
    protected final Map<Integer, CellType> mCellTypesByViewType = new HashMap<Integer, CellType>();
    protected final Map<CellFactory, Integer> mMyViewTypesByCell = new HashMap<CellFactory, Integer>();
    protected final Map<CellFactory, Integer> mTheirViewTypesByCell = new HashMap<CellFactory, Integer>();

    // Dates and Clustering
    private final Map<Uri, Cluster> mClusterCache = new HashMap<Uri, Cluster>();
    private final DateFormat mDateFormat;
    private final DateFormat mTimeFormat;

    private View mFooterView;
    private int mFooterPosition = 0;

    private Integer mRecipientStatusPosition;
    private boolean mReadReceiptsEnabled = true;


    //Style
    private MessageStyle mMessageStyle;

    private RecyclerView mRecyclerView;

    protected boolean mShouldShowAvatarInOneOnOneConversations;
    protected boolean mShouldShowAvatarPresence = true;
    private ImageCacheWrapper mImageCacheWrapper;

    public MessagesAdapter(Context context, LayerClient layerClient, ImageCacheWrapper imageCacheWrapper) {
        mImageCacheWrapper = imageCacheWrapper;
        mLayerClient = layerClient;
        mLayoutInflater = LayoutInflater.from(context);
        mUiThreadHandler = new Handler(Looper.getMainLooper());
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
        mDisplayMetrics = context.getResources().getDisplayMetrics();

        mQueryController = layerClient.newRecyclerViewController(null, null, this);
        mQueryController.setPreProcessCallback(new ListViewController.PreProcessCallback<Message>() {
            @Override
            public void onCache(ListViewController listViewController, Message message) {
                for (CellFactory factory : mCellFactories) {
                    if (factory.isBindable(message)) {
                        factory.getParsedContent(mLayerClient, message);
                        break;
                    }
                }
            }
        });

        setHasStableIds(false);

        mIdentityEventListener = new IdentityRecyclerViewEventListener(this);
        mLayerClient.registerEventListener(mIdentityEventListener);
    }

    /**
     * Sets this MessagesAdapter's Message Query.
     *
     * @param query Query drive this MessagesAdapter.
     * @return This MessagesAdapter.
     */
    public MessagesAdapter setQuery(Query<Message> query) {
        mQueryController.setQuery(query);
        return this;
    }

    /**
     * Refreshes this adapter by re-running the underlying Query.
     */
    public void refresh() {
        mQueryController.execute();
    }

    /**
     * Performs cleanup when the Activity/Fragment using the adapter is destroyed.
     */
    public void onDestroy() {
        mLayerClient.unregisterEventListener(mIdentityEventListener);
    }

    public MessagesAdapter setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        return this;
    }

    public void setStyle(MessageStyle messageStyle) {
        this.mMessageStyle = messageStyle;
    }

    public void setFooterView(View footerView) {
        boolean isNull = footerView == null;
        boolean wasNull = mFooterView == null;
        mFooterView = footerView;

        if (wasNull && !isNull) {
            // Insert
            notifyItemInserted(mFooterPosition);
        } else if (!wasNull && isNull) {
            // Delete
            notifyItemRemoved(mFooterPosition);
        } else if (!wasNull && !isNull) {
            // Change
            notifyItemChanged(mFooterPosition);
        }
    }

    public View getFooterView() {
        return mFooterView;
    }

    /**
     * @return If the AvatarViewModel for the other participant in a one on one conversation  will be shown
     * or not
     */
    public boolean getShouldShowAvatarInOneOnOneConversations() {
        return mShouldShowAvatarInOneOnOneConversations;
    }

    /**
     * @param shouldShowAvatarInOneOnOneConversations Whether the AvatarViewModel for the other participant
     *                                                in a one on one conversation should be shown
     *                                                or not
     */
    public void setShouldShowAvatarInOneOnOneConversations(boolean shouldShowAvatarInOneOnOneConversations) {
        mShouldShowAvatarInOneOnOneConversations = shouldShowAvatarInOneOnOneConversations;
    }

    /**
     * @return If the AvatarViewModel for the other participant in a one on one conversation will be shown
     * or not. Defaults to `true`.
     */
    public boolean getShouldShowAvatarPresence() {
        return mShouldShowAvatarPresence;
    }

    /**
     * @param shouldShowPresence Whether the AvatarView for the other participant in a one on one
     *                           conversation should be shown or not. Default is `true`.
     */
    public MessagesAdapter setShouldShowAvatarPresence(boolean shouldShowPresence) {
        mShouldShowAvatarPresence = shouldShowPresence;
        return this;
    }

    /**
     * Set whether or not the conversation supports read receipts. This determines if the read
     * receipts should be shown in the view holders.
     *
     * @param readReceiptsEnabled true if the conversation is adapter is used for supports read receipts
     */
    public void setReadReceiptsEnabled(boolean readReceiptsEnabled) {
        mReadReceiptsEnabled = readReceiptsEnabled;
    }


    //==============================================================================================
    // Listeners
    //==============================================================================================

    /**
     * Sets the OnAppendListener for this AtlasQueryAdapter.  The listener will be called when items
     * are appended to the end of this adapter.  This is useful for implementing a scroll-to-bottom
     * feature.
     *
     * @param listener The OnAppendListener to notify about appended items.
     * @return This AtlasQueryAdapter.
     */
    public MessagesAdapter setOnMessageAppendListener(OnMessageAppendListener listener) {
        mAppendListener = listener;
        return this;
    }


    //==============================================================================================
    // Adapter and Cells
    //==============================================================================================

    /**
     * Registers one or more CellFactories for the MessagesAdapter to manage.  CellFactories
     * know which Messages they can render, and handle View caching, creation, and binding.
     *
     * @param cellFactories Cells to register.
     * @return This MessagesAdapter.
     */
    public MessagesAdapter addCellFactories(CellFactory... cellFactories) {
        for (CellFactory cellFactory : cellFactories) {
            cellFactory.setStyle(mMessageStyle);
            mCellFactories.add(cellFactory);

            mViewTypeCount++;
            CellType me = new CellType(true, cellFactory);
            mCellTypesByViewType.put(mViewTypeCount, me);
            mMyViewTypesByCell.put(cellFactory, mViewTypeCount);

            mViewTypeCount++;
            CellType notMe = new CellType(false, cellFactory);
            mCellTypesByViewType.put(mViewTypeCount, notMe);
            mTheirViewTypesByCell.put(cellFactory, mViewTypeCount);
        }
        return this;
    }

    public Set<CellFactory> getCellFactories() {
        return mCellFactories;
    }

    @Override
    public int getItemViewType(int position) {
        if (mFooterView != null && position == mFooterPosition) return VIEW_TYPE_FOOTER;
        Message message = getItem(position);
        Identity authenticatedUser = mLayerClient.getAuthenticatedUser();
        boolean isMe = authenticatedUser != null && authenticatedUser.equals(message.getSender());
        for (CellFactory factory : mCellFactories) {
            if (!factory.isBindable(message)) continue;
            return isMe ? mMyViewTypesByCell.get(factory) : mTheirViewTypesByCell.get(factory);
        }
        return -1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FOOTER) {
            return new ViewHolder(mLayoutInflater.inflate(ViewHolder.RESOURCE_ID_FOOTER, parent, false));
        }

        CellType cellType = mCellTypesByViewType.get(viewType);

        ViewDataBinding viewDataBinding = null;
        if (cellType.mMe) {
           viewDataBinding = UiMessageItemMeBinding.inflate(mLayoutInflater, parent, false);
        } else {
            viewDataBinding = UiMessageItemThemBinding.inflate(mLayoutInflater, parent, false);
        }

        CellViewHolder rootViewHolder = new CellViewHolder(viewDataBinding, mShouldShowAvatarPresence, mImageCacheWrapper);
        rootViewHolder.mCellHolder = cellType.mCellFactory.createCellHolder(rootViewHolder.getCell(), cellType.mMe, mLayoutInflater);
        rootViewHolder.mCellHolderSpecs = new CellFactory.CellHolderSpecs();
        return rootViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        mQueryController.updateBoundPosition(position);
        if (mFooterView != null && position == mFooterPosition) {
            // Footer
            bindFooter(viewHolder);
        } else {
            // Cell
            bindCellViewHolder((CellViewHolder) viewHolder, position);
        }
    }

    public void bindFooter(ViewHolder viewHolder) {
        viewHolder.mRoot.removeAllViews();
        if (mFooterView.getParent() != null) {
            ((ViewGroup) mFooterView.getParent()).removeView(mFooterView);
        }
        viewHolder.mRoot.addView(mFooterView);
    }

    public void bindCellViewHolder(CellViewHolder viewHolder, int position) {
        Message message = getItem(position);
        viewHolder.mMessage = message;
        CellType cellType = mCellTypesByViewType.get(viewHolder.getItemViewType());
        boolean oneOnOne = message.getConversation().getParticipants().size() == 2;

        // Clustering and dates
        Cluster cluster = getClustering(message, position);


        MessageItemViewModel messageItemViewModel = new MessageItemViewModel(viewHolder.getCell().getContext(),
                mLayerClient, message, cluster, oneOnOne, position, mShouldShowAvatarInOneOnOneConversations,
                mRecipientStatusPosition, mReadReceiptsEnabled, cellType.mMe);
        viewHolder.bind(messageItemViewModel);

        if (!oneOnOne && (cluster.mClusterWithNext == null || cluster.mClusterWithNext != ClusterType.LESS_THAN_MINUTE)) {
            // Add the position to the positions map for Identity updates
            mIdentityEventListener.addIdentityPosition(position, Collections.singleton(message.getSender()));
        }

        // CellHolder
        CellFactory.CellHolder cellHolder = viewHolder.mCellHolder;
        cellHolder.setMessage(message);

        // Cell dimensions
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.getCell().getLayoutParams();
        int maxWidth = mRecyclerView.getWidth() - viewHolder.mRoot.getPaddingLeft() - viewHolder.mRoot.getPaddingRight() - params.leftMargin - params.rightMargin;
        if (!oneOnOne && !cellType.mMe) {
            // Subtract off avatar width if needed
            ViewGroup.MarginLayoutParams avatarParams = (ViewGroup.MarginLayoutParams) viewHolder.getAvatarView().getLayoutParams();
            maxWidth -= avatarParams.width + avatarParams.rightMargin + avatarParams.leftMargin;
        }
        // TODO: subtract spacing rather than multiply by 0.8 to handle screen sizes more cleanly
        int maxHeight = (int) viewHolder.mRoot.getContext().getResources().getDimension(R.dimen.layer_ui_messages_max_cell_height);

        viewHolder.mCellHolderSpecs.isMe = cellType.mMe;
        viewHolder.mCellHolderSpecs.position = position;
        viewHolder.mCellHolderSpecs.maxWidth = maxWidth;
        viewHolder.mCellHolderSpecs.maxHeight = maxHeight;
        cellType.mCellFactory.bindCellHolder(cellHolder, cellType.mCellFactory.getParsedContent(mLayerClient, message), message, viewHolder.mCellHolderSpecs);
    }


    @Override
    public int getItemCount() {
        return mQueryController.getItemCount() + ((mFooterView == null) ? 0 : 1);
    }

    @Override
    public Integer getPosition(Message message) {
        return mQueryController.getPosition(message);
    }

    @Override
    public Integer getPosition(Message message, int lastPosition) {
        return mQueryController.getPosition(message, lastPosition);
    }

    @Override
    public Message getItem(int position) {
        if (mFooterView != null && position == mFooterPosition) return null;
        return mQueryController.getItem(position);
    }

    @Override
    public Message getItem(RecyclerView.ViewHolder viewHolder) {
        if (!(viewHolder instanceof CellViewHolder)) return null;
        return ((CellViewHolder) viewHolder).mMessage;
    }

    //==============================================================================================
    // Clustering
    //==============================================================================================

    // TODO: optimize by limiting search to positions in- and around- visible range
    private Cluster getClustering(Message message, int position) {
        Cluster result = mClusterCache.get(message.getId());
        if (result == null) {
            result = new Cluster();
            mClusterCache.put(message.getId(), result);
        }

        int previousPosition = position - 1;
        Message previousMessage = (previousPosition >= 0) ? getItem(previousPosition) : null;
        if (previousMessage != null) {
            result.mDateBoundaryWithPrevious = isDateBoundary(previousMessage.getReceivedAt(), message.getReceivedAt());
            result.mClusterWithPrevious = ClusterType.fromMessages(previousMessage, message);

            Cluster previousCluster = mClusterCache.get(previousMessage.getId());
            if (previousCluster == null) {
                previousCluster = new Cluster();
                mClusterCache.put(previousMessage.getId(), previousCluster);
            } else {
                // does the previous need to change its clustering?
                if ((previousCluster.mClusterWithNext != result.mClusterWithPrevious) ||
                        (previousCluster.mDateBoundaryWithNext != result.mDateBoundaryWithPrevious)) {
                    requestUpdate(previousMessage, previousPosition);
                }
            }
            previousCluster.mClusterWithNext = result.mClusterWithPrevious;
            previousCluster.mDateBoundaryWithNext = result.mDateBoundaryWithPrevious;
        }

        int nextPosition = position + 1;
        Message nextMessage = (nextPosition < getItemCount()) ? getItem(nextPosition) : null;
        if (nextMessage != null) {
            result.mDateBoundaryWithNext = isDateBoundary(message.getReceivedAt(), nextMessage.getReceivedAt());
            result.mClusterWithNext = ClusterType.fromMessages(message, nextMessage);

            Cluster nextCluster = mClusterCache.get(nextMessage.getId());
            if (nextCluster == null) {
                nextCluster = new Cluster();
                mClusterCache.put(nextMessage.getId(), nextCluster);
            } else {
                // does the next need to change its clustering?
                if ((nextCluster.mClusterWithPrevious != result.mClusterWithNext) ||
                        (nextCluster.mDateBoundaryWithPrevious != result.mDateBoundaryWithNext)) {
                    requestUpdate(nextMessage, nextPosition);
                }
            }
            nextCluster.mClusterWithPrevious = result.mClusterWithNext;
            nextCluster.mDateBoundaryWithPrevious = result.mDateBoundaryWithNext;
        }

        return result;
    }

    private static boolean isDateBoundary(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        return (d1.getYear() != d2.getYear()) || (d1.getMonth() != d2.getMonth()) || (d1.getDay() != d2.getDay());
    }

    private void requestUpdate(final Message message, final int lastPosition) {
        mUiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemChanged(getPosition(message, lastPosition));
            }
        });
    }


    //==============================================================================================
    // Read and delivery receipts
    //==============================================================================================

    private void updateRecipientStatusPosition() {
        if (mReadReceiptsEnabled) {
            Integer oldPosition = mRecipientStatusPosition;
            // Set new position to last in the list
            mRecipientStatusPosition = mQueryController.getItemCount() - 1;
            if (oldPosition != null) {
                notifyItemChanged(oldPosition);
            }
        }
    }


    //==============================================================================================
    // UI update callbacks
    //==============================================================================================

    @Override
    public void onQueryDataSetChanged(RecyclerViewController controller) {
        mFooterPosition = mQueryController.getItemCount();
        updateRecipientStatusPosition();
        notifyDataSetChanged();

        if (Log.isPerfLoggable()) {
            Log.perf("Messages adapter - onQueryDataSetChanged");
        }
    }

    @Override
    public void onQueryItemChanged(RecyclerViewController controller, int position) {
        notifyItemChanged(position);

        if (Log.isPerfLoggable()) {
            Log.perf("Messages adapter - onQueryItemChanged. Position: " + position);
        }
    }

    @Override
    public void onQueryItemRangeChanged(RecyclerViewController controller, int positionStart, int itemCount) {
        notifyItemRangeChanged(positionStart, itemCount);

        if (Log.isPerfLoggable()) {
            Log.perf("Messages adapter - onQueryItemRangeChanged. Position start: " + positionStart + " Count: " + itemCount);
        }
    }

    @Override
    public void onQueryItemInserted(RecyclerViewController controller, int position) {
        mFooterPosition++;
        updateRecipientStatusPosition();
        notifyItemInserted(position);
        if (mAppendListener != null && (position + 1) == getItemCount()) {
            mAppendListener.onMessageAppend(this, getItem(position));
        }

        if (Log.isPerfLoggable()) {
            Log.perf("Messages adapter - onQueryItemInserted. Position: " + position);
        }
    }

    @Override
    public void onQueryItemRangeInserted(RecyclerViewController controller, int positionStart, int itemCount) {
        mFooterPosition += itemCount;
        updateRecipientStatusPosition();
        notifyItemRangeInserted(positionStart, itemCount);
        int positionEnd = positionStart + itemCount;
        if (mAppendListener != null && (positionEnd + 1) == getItemCount()) {
            mAppendListener.onMessageAppend(this, getItem(positionEnd));
        }

        if (Log.isPerfLoggable()) {
            Log.perf("Messages adapter - onQueryItemRangeInserted. Position start: " + positionStart + " Count: " + itemCount);
        }
    }

    @Override
    public void onQueryItemRemoved(RecyclerViewController controller, int position) {
        mFooterPosition--;
        updateRecipientStatusPosition();
        notifyItemRemoved(position);

        if (Log.isPerfLoggable()) {
            Log.perf("Messages adapter - onQueryItemRemoved. Position: " + position);
        }
    }

    @Override
    public void onQueryItemRangeRemoved(RecyclerViewController controller, int positionStart, int itemCount) {
        mFooterPosition -= itemCount;
        updateRecipientStatusPosition();
        notifyItemRangeRemoved(positionStart, itemCount);

        if (Log.isPerfLoggable()) {
            Log.perf("Messages adapter - onQueryItemRangeRemoved. Position start: " + positionStart + " Count: " + itemCount);
        }
    }

    @Override
    public void onQueryItemMoved(RecyclerViewController controller, int fromPosition, int toPosition) {
        updateRecipientStatusPosition();
        notifyItemMoved(fromPosition, toPosition);

        if (Log.isPerfLoggable()) {
            Log.perf("Conversations adapter - onQueryItemMoved. From: " + fromPosition + " To: " + toPosition);
        }
    }


    //==============================================================================================
    // Inner classes
    //==============================================================================================

    static class ViewHolder extends RecyclerView.ViewHolder {
        public final static int RESOURCE_ID_FOOTER = R.layout.ui_message_item_footer;

        // View cache
        protected ViewGroup mRoot;

        public ViewHolder(View itemView) {
            super(itemView);
            mRoot = (ViewGroup) itemView.findViewById(R.id.swipeable);
        }
    }

    static class CellViewHolder extends ViewHolder {

        protected Message mMessage;

        // Cell
        protected CellFactory.CellHolder mCellHolder;
        protected CellFactory.CellHolderSpecs mCellHolderSpecs;
        private ViewDataBinding mViewDataBinding;

        //TODO: get rid of mShouldShowAvatarPresence, mImageCacheWrapper
        public CellViewHolder(ViewDataBinding viewDataBinding, boolean shouldShowAvatarPresence, ImageCacheWrapper imageCachWrapper) {
            super(viewDataBinding.getRoot());
            mViewDataBinding = viewDataBinding;

            if (mViewDataBinding instanceof UiMessageItemThemBinding) {
                UiMessageItemThemBinding uiMessageItemThemBinding = (UiMessageItemThemBinding) mViewDataBinding;
                AvatarView avatarView = ((UiMessageItemThemBinding) mViewDataBinding).avatar;
                if (avatarView != null)  {
                    avatarView.init(new AvatarViewModelImpl(imageCachWrapper), new IdentityNameFormatterImpl());
                }

                if (!shouldShowAvatarPresence) {
                    PresenceView presenceView = uiMessageItemThemBinding.presence;
                    presenceView.setVisibility(View.INVISIBLE);
                }
            }
        }

        public void bind(MessageItemViewModel messageItemViewModel) {
            if (mViewDataBinding instanceof UiMessageItemThemBinding) {
                UiMessageItemThemBinding uiMessageItemThemBinding =
                        (UiMessageItemThemBinding) mViewDataBinding;
                uiMessageItemThemBinding.setViewModel(messageItemViewModel);
            } else {
                UiMessageItemMeBinding uiMessageItemMeBinding = ((UiMessageItemMeBinding) mViewDataBinding);
                uiMessageItemMeBinding.setViewModel(messageItemViewModel);
            }

            mViewDataBinding.executePendingBindings();
        }

        public ViewGroup getCell() {
            return mViewDataBinding instanceof UiMessageItemThemBinding
                    ? ((UiMessageItemThemBinding) mViewDataBinding).cell
                    : ((UiMessageItemMeBinding) mViewDataBinding).cell;
        }

        public AvatarView getAvatarView() {
            if (mViewDataBinding instanceof UiMessageItemThemBinding) {
                return ((UiMessageItemThemBinding) mViewDataBinding).avatar;
            }

            return null;
        }

    }

    public enum ClusterType {
        NEW_SENDER,
        LESS_THAN_MINUTE,
        LESS_THAN_HOUR,
        MORE_THAN_HOUR;

        private static final long MILLIS_MINUTE = 60 * 1000;
        private static final long MILLIS_HOUR = 60 * MILLIS_MINUTE;

        public static ClusterType fromMessages(Message older, Message newer) {
            // Different users?
            if (!older.getSender().equals(newer.getSender())) return NEW_SENDER;

            // Time clustering for same user?
            Date oldReceivedAt = older.getReceivedAt();
            Date newReceivedAt = newer.getReceivedAt();
            if (oldReceivedAt == null || newReceivedAt == null) return LESS_THAN_MINUTE;
            long delta = Math.abs(newReceivedAt.getTime() - oldReceivedAt.getTime());
            if (delta <= MILLIS_MINUTE) return LESS_THAN_MINUTE;
            if (delta <= MILLIS_HOUR) return LESS_THAN_HOUR;
            return MORE_THAN_HOUR;
        }
    }

    public static class Cluster {
        public boolean mDateBoundaryWithPrevious;
        public ClusterType mClusterWithPrevious;

        public boolean mDateBoundaryWithNext;
        public ClusterType mClusterWithNext;
    }

    private static class MessagePosition {
        public Message mMessage;
        public int mPosition;

        public MessagePosition(Message message, int position) {
            mMessage = message;
            mPosition = position;
        }
    }

    private static class CellType {
        protected final boolean mMe;
        protected final CellFactory mCellFactory;

        public CellType(boolean me, CellFactory CellFactory) {
            mMe = me;
            mCellFactory = CellFactory;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CellType cellType = (CellType) o;

            if (mMe != cellType.mMe) return false;
            return mCellFactory.equals(cellType.mCellFactory);

        }

        @Override
        public int hashCode() {
            int result = (mMe ? 1 : 0);
            result = 31 * result + mCellFactory.hashCode();
            return result;
        }
    }

    /**
     * Listens for inserts to the end of an AtlasQueryAdapter.
     */
    public interface OnMessageAppendListener {
        /**
         * Alerts the listener to inserts at the end of an AtlasQueryAdapter.  If a batch of items
         * were appended, only the last one will be alerted here.
         *
         * @param adapter The AtlasQueryAdapter which had an item appended.
         * @param message The item appended to the AtlasQueryAdapter.
         */
        void onMessageAppend(MessagesAdapter adapter, Message message);
    }
}
