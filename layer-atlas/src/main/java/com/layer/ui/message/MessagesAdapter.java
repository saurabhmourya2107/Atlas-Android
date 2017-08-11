package com.layer.ui.message;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
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
import com.layer.ui.adapters.ItemRecyclerViewAdapter;
import com.layer.ui.adapters.ItemViewHolder;
import com.layer.ui.databinding.UiMessageItemBinding;
import com.layer.ui.databinding.UiMessageItemFooterBinding;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.message.messagetypes.MessageStyle;
import com.layer.ui.util.DateFormatter;
import com.layer.ui.util.IdentityRecyclerViewEventListener;
import com.layer.ui.util.Log;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
public class MessagesAdapter extends ItemRecyclerViewAdapter<Message, MessageItemViewModel,
        ViewDataBinding, MessageStyle, ItemViewHolder<Message, MessageItemViewModel, ViewDataBinding, MessageStyle>> {

    private static final String TAG = MessagesAdapter.class.getSimpleName();
    private final static int VIEW_TYPE_FOOTER = 0;
    protected final Handler mUiThreadHandler;
    protected final DisplayMetrics mDisplayMetrics;
    protected final Set<CellFactory> mCellFactories = new LinkedHashSet<CellFactory>();
    protected final Map<Integer, MessageCell> mCellTypesByViewType = new HashMap<Integer, MessageCell>();
    protected final Map<CellFactory, Integer> mMyViewTypesByCell =
            new HashMap<CellFactory, Integer>();
    protected final Map<CellFactory, Integer> mTheirViewTypesByCell =
            new HashMap<CellFactory, Integer>();
    private final IdentityRecyclerViewEventListener mIdentityEventListener;
    // Dates and Clustering
    private final Map<Uri, MessageCluster> mClusterCache = new HashMap<>();
    protected OnMessageAppendListener mAppendListener;
    // Cells
    protected int mViewTypeCount = VIEW_TYPE_FOOTER;
    protected boolean mShouldShowAvatarInOneOnOneConversations;
    protected boolean mShouldShowAvatarPresence = true;
    private View mFooterView;
    private int mFooterPosition = 0;
    private Integer mRecipientStatusPosition;
    private boolean mReadReceiptsEnabled = true;
    private ImageCacheWrapper mImageCacheWrapper;

    private int readCount = 0;
    private boolean delivered = false;
    private Map<Identity, Message.RecipientStatus> statuses;
    private DateFormatter mDateFormatter;

    public MessagesAdapter(Context context, LayerClient layerClient, ImageCacheWrapper imageCacheWrapper, DateFormatter dateFormatter) {
        super(context, layerClient, TAG, false);
        mImageCacheWrapper = imageCacheWrapper;
        mDateFormatter = dateFormatter;
        mUiThreadHandler = new Handler(Looper.getMainLooper());
        mDisplayMetrics = context.getResources().getDisplayMetrics();

        mQueryController = layerClient.newRecyclerViewController(null, null, this);
        mQueryController.setPreProcessCallback(
                new ListViewController.PreProcessCallback<Message>() {
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
        mIdentityEventListener = new IdentityRecyclerViewEventListener(this);
        mLayerClient.registerEventListener(mIdentityEventListener);
    }


    private static boolean isDateBoundary(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        return (d1.getYear() != d2.getYear()) || (d1.getMonth() != d2.getMonth()) || (d1.getDay()
                != d2.getDay());
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
     * Performs cleanup when the Activity/Fragment using the adapter is destroyed.
     */
    public void onDestroy() {
        mLayerClient.unregisterEventListener(mIdentityEventListener);
    }

    public MessagesAdapter setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        return this;
    }



    public View getFooterView() {
        return mFooterView;
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

    /**
     * @return If the AvatarViewModel for the other participant in a one on one conversation  will
     * be shown
     * or not
     */
    public boolean getShouldShowAvatarInOneOnOneConversations() {
        return mShouldShowAvatarInOneOnOneConversations;
    }

    /**
     * @param shouldShowAvatarInOneOnOneConversations Whether the AvatarViewModel for the other
     *                                                participant
     *                                                in a one on one conversation should be shown
     *                                                or not
     */
    public void setShouldShowAvatarInOneOnOneConversations(
            boolean shouldShowAvatarInOneOnOneConversations) {
        mShouldShowAvatarInOneOnOneConversations = shouldShowAvatarInOneOnOneConversations;
    }

    /**
     * @return If the AvatarViewModel for the other participant in a one on one conversation will be
     * shown
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


    //==============================================================================================
    // Listeners
    //==============================================================================================

    /**
     * Set whether or not the conversation supports read receipts. This determines if the read
     * receipts should be shown in the view holders.
     *
     * @param readReceiptsEnabled true if the conversation is adapter is used for supports read
     *                            receipts
     */
    public void setReadReceiptsEnabled(boolean readReceiptsEnabled) {
        mReadReceiptsEnabled = readReceiptsEnabled;
    }


    //==============================================================================================
    // Adapter and Cells
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

    /**
     * Registers one or more CellFactories for the MessagesAdapter to manage.  CellFactories
     * know which Messages they can render, and handle View caching, creation, and mBinding.
     *
     * @param cellFactories Cells to register.
     * @return This MessagesAdapter.
     */
    public MessagesAdapter addCellFactories(List<CellFactory> cellFactories) {
        for (CellFactory cellFactory : cellFactories) {
            cellFactory.setStyle(getStyle());
            mCellFactories.add(cellFactory);

            mViewTypeCount++;
            MessageCell me = new MessageCell(true, cellFactory);
            mCellTypesByViewType.put(mViewTypeCount, me);
            mMyViewTypesByCell.put(cellFactory, mViewTypeCount);

            mViewTypeCount++;
            MessageCell notMe = new MessageCell(false, cellFactory);
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
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_FOOTER) {
            UiMessageItemFooterBinding uiMessageItemFooterBinding =
                    UiMessageItemFooterBinding.inflate(mLayoutInflater, parent, false);
            return new MessageItemFooterViewHolder(uiMessageItemFooterBinding);
        }

        MessageCell messageCell = mCellTypesByViewType.get(viewType);

        UiMessageItemBinding uiMessageItemMeBinding = UiMessageItemBinding.inflate(mLayoutInflater, parent, false);

        MessageItemViewModel messageItemViewModel = new MessageItemViewModel(null);
        MessageCellViewHolder rootViewHolder = new MessageCellViewHolder(uiMessageItemMeBinding, messageItemViewModel,
                mImageCacheWrapper);
        rootViewHolder.mCellHolder = messageCell.mCellFactory.createCellHolder(
                rootViewHolder.getCell(), messageCell.mMe, mLayoutInflater);
        rootViewHolder.mCellHolderSpecs = new CellFactory.CellHolderSpecs();
        return rootViewHolder;
    }

    @Override
    public void onBindViewHolder(
            ItemViewHolder<Message, MessageItemViewModel, ViewDataBinding, MessageStyle> holder,
            int position, List<Object> payloads) {
        if (mFooterView != null && position == mFooterPosition) {
            // Footer
            bindFooter((MessageItemFooterViewHolder) holder);
        } else {
            // Cell
            bindCellViewHolder((MessageCellViewHolder) holder, position);
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    public void bindFooter(MessageItemFooterViewHolder viewHolder) {
        viewHolder.mRoot.removeAllViews();
        if (mFooterView.getParent() != null) {
            ((ViewGroup) mFooterView.getParent()).removeView(mFooterView);
        }
        viewHolder.mRoot.addView(mFooterView);
    }

    public void bindCellViewHolder(MessageCellViewHolder viewHolder, int position) {
        Message message = getItem(position);
        viewHolder.mMessage = message;
        MessageCell messageCell = mCellTypesByViewType.get(viewHolder.getItemViewType());
        boolean oneOnOne = message.getConversation().getParticipants().size() == 2;

        // Clustering and dates
        MessageCluster messageCluster = getClustering(message, position);
        updateValuesForRecipient(messageCell.mMe, position, message);

        boolean isClusterSpaceVisible = messageCluster.mClusterWithPrevious == MessageCluster.Type.NEW_SENDER
                || messageCluster.mClusterWithPrevious == MessageCluster.Type.LESS_THAN_HOUR;
        boolean shouldDisplayName = !messageCell.mMe && (!oneOnOne && (
                messageCluster.mClusterWithPrevious == null
                || messageCluster.mClusterWithPrevious == MessageCluster.Type.NEW_SENDER));
        boolean shouldBindDateTimeForMessage =
                messageCluster.mClusterWithPrevious == null || (messageCluster.mDateBoundaryWithPrevious
                        || messageCluster.mClusterWithPrevious == MessageCluster.Type.MORE_THAN_HOUR);
        boolean shouldClusterBeVisible = messageCluster.mClusterWithNext == null
                || messageCluster.mClusterWithNext != MessageCluster.Type.LESS_THAN_MINUTE;

        boolean isRecipientStatusVisible =
                messageCell.mMe && (mReadReceiptsEnabled && mRecipientStatusPosition != null
                        && mRecipientStatusPosition == position);

        Context context = viewHolder.getCell().getContext();
        String str = "";

        if (messageCell.mMe && (mReadReceiptsEnabled && mRecipientStatusPosition != null
                && mRecipientStatusPosition == position)) {
            if (readCount > 0) {
                // Use 2 to include one other participant plus the current user
                if (statuses.size() > 2) {
                    str = context.getResources()
                            .getQuantityString(
                                    R.plurals.layer_ui_message_item_read_muliple_participants,
                                    readCount, readCount);
                } else {
                    str = context.getString(R.string.layer_ui_message_item_read);
                }
            } else if (delivered) {
                str = context.getString(R.string.layer_ui_message_item_delivered);
            }
        }

        boolean isAvatarViewVisibilitySet = false;
        int avatarViewVisibilityType = 0;

        if (messageCell.mMe || (oneOnOne && !mShouldShowAvatarInOneOnOneConversations)) {
            avatarViewVisibilityType = View.GONE;
            isAvatarViewVisibilitySet = true;
        }

        if (!isAvatarViewVisibilitySet && ((oneOnOne && mShouldShowAvatarInOneOnOneConversations) || shouldClusterBeVisible)) {
            avatarViewVisibilityType = View.VISIBLE;
            isAvatarViewVisibilitySet = true;
        }

        if (!isAvatarViewVisibilitySet) {
            avatarViewVisibilityType = View.INVISIBLE;
        }

        viewHolder.bind(message, avatarViewVisibilityType, isClusterSpaceVisible, shouldDisplayName,
                shouldBindDateTimeForMessage, str, isRecipientStatusVisible, mDateFormatter, messageCell.mMe);

        if (!oneOnOne && (messageCluster.mClusterWithNext == null
                || messageCluster.mClusterWithNext != MessageCluster.Type.LESS_THAN_MINUTE)) {
            // Add the position to the positions map for Identity updates
            mIdentityEventListener.addIdentityPosition(position,
                    Collections.singleton(message.getSender()));
        }

        // CellHolder
        CellFactory.CellHolder cellHolder = viewHolder.mCellHolder;
        cellHolder.setMessage(message);

        // Cell dimensions
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) viewHolder.getCell().getLayoutParams();
        int maxWidth = mRecyclerView.getWidth() - viewHolder.mRoot.getPaddingLeft()
                - viewHolder.mRoot.getPaddingRight() - params.leftMargin - params.rightMargin;
        if (!oneOnOne && !messageCell.mMe) {
            // Subtract off avatar width if needed
            ViewGroup.MarginLayoutParams avatarParams =
                    (ViewGroup.MarginLayoutParams) viewHolder.getAvatarView().getLayoutParams();
            maxWidth -= avatarParams.width + avatarParams.rightMargin + avatarParams.leftMargin;
        }
        // TODO: subtract spacing rather than multiply by 0.8 to handle screen sizes more cleanly
        int maxHeight = (int) viewHolder.mRoot.getContext().getResources().getDimension(
                R.dimen.layer_ui_messages_max_cell_height);

        viewHolder.mCellHolderSpecs.isMe = messageCell.mMe;
        viewHolder.mCellHolderSpecs.position = position;
        viewHolder.mCellHolderSpecs.maxWidth = maxWidth;
        viewHolder.mCellHolderSpecs.maxHeight = maxHeight;
        messageCell.mCellFactory.bindCellHolder(cellHolder,
                messageCell.mCellFactory.getParsedContent(mLayerClient, message), message,
                viewHolder.mCellHolderSpecs);
    }

    private void updateValuesForRecipient(boolean isCellTypeMe, int position, Message message) {
        if (isCellTypeMe && (mReadReceiptsEnabled && mRecipientStatusPosition != null
                && mRecipientStatusPosition == position)) {
            statuses = message.getRecipientStatus();
            for (Map.Entry<Identity, Message.RecipientStatus> entry : statuses.entrySet()) {
                // Only show receipts for other members
                if (entry.getKey().equals(mLayerClient.getAuthenticatedUser())) continue;
                // Skip receipts for members no longer in the conversation
                if (entry.getValue() == null) continue;

                switch (entry.getValue()) {
                    case READ:
                        readCount++;
                        break;
                    case DELIVERED:
                        delivered = true;
                        break;
                }
            }
        }
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

    //==============================================================================================
    // Clustering
    //==============================================================================================

    // TODO: optimize by limiting search to positions in- and around- visible range
    private MessageCluster getClustering(Message message, int position) {
        MessageCluster result = mClusterCache.get(message.getId());
        if (result == null) {
            result = new MessageCluster();
            mClusterCache.put(message.getId(), result);
        }

        int previousPosition = position - 1;
        Message previousMessage = (previousPosition >= 0) ? getItem(previousPosition) : null;
        if (previousMessage != null) {
            result.mDateBoundaryWithPrevious = isDateBoundary(previousMessage.getReceivedAt(),
                    message.getReceivedAt());
            result.mClusterWithPrevious = MessageCluster.Type.fromMessages(previousMessage, message);

            MessageCluster previousMessageCluster = mClusterCache.get(previousMessage.getId());
            if (previousMessageCluster == null) {
                previousMessageCluster = new MessageCluster();
                mClusterCache.put(previousMessage.getId(), previousMessageCluster);
            } else {
                // does the previous need to change its clustering?
                if ((previousMessageCluster.mClusterWithNext != result.mClusterWithPrevious) ||
                        (previousMessageCluster.mDateBoundaryWithNext
                                != result.mDateBoundaryWithPrevious)) {
                    requestUpdate(previousMessage, previousPosition);
                }
            }
            previousMessageCluster.mClusterWithNext = result.mClusterWithPrevious;
            previousMessageCluster.mDateBoundaryWithNext = result.mDateBoundaryWithPrevious;
        }

        int nextPosition = position + 1;
        Message nextMessage = (nextPosition < getItemCount()) ? getItem(nextPosition) : null;
        if (nextMessage != null) {
            result.mDateBoundaryWithNext = isDateBoundary(message.getReceivedAt(),
                    nextMessage.getReceivedAt());
            result.mClusterWithNext = MessageCluster.Type.fromMessages(message, nextMessage);

            MessageCluster nextMessageCluster = mClusterCache.get(nextMessage.getId());
            if (nextMessageCluster == null) {
                nextMessageCluster = new MessageCluster();
                mClusterCache.put(nextMessage.getId(), nextMessageCluster);
            } else {
                // does the next need to change its clustering?
                if ((nextMessageCluster.mClusterWithPrevious != result.mClusterWithNext) ||
                        (nextMessageCluster.mDateBoundaryWithPrevious != result.mDateBoundaryWithNext)) {
                    requestUpdate(nextMessage, nextPosition);
                }
            }
            nextMessageCluster.mClusterWithPrevious = result.mClusterWithNext;
            nextMessageCluster.mDateBoundaryWithPrevious = result.mDateBoundaryWithNext;
        }

        return result;
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
    public void onQueryItemRangeChanged(RecyclerViewController controller, int positionStart,
            int itemCount) {
        notifyItemRangeChanged(positionStart, itemCount);

        if (Log.isPerfLoggable()) {
            Log.perf("Messages adapter - onQueryItemRangeChanged. Position start: " + positionStart
                    + " Count: " + itemCount);
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
    public void onQueryItemRangeInserted(RecyclerViewController controller, int positionStart,
            int itemCount) {
        mFooterPosition += itemCount;
        updateRecipientStatusPosition();
        notifyItemRangeInserted(positionStart, itemCount);
        int positionEnd = positionStart + itemCount;
        if (mAppendListener != null && (positionEnd + 1) == getItemCount()) {
            mAppendListener.onMessageAppend(this, getItem(positionEnd));
        }

        if (Log.isPerfLoggable()) {
            Log.perf("Messages adapter - onQueryItemRangeInserted. Position start: " + positionStart
                    + " Count: " + itemCount);
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
    public void onQueryItemRangeRemoved(RecyclerViewController controller, int positionStart,
            int itemCount) {
        mFooterPosition -= itemCount;
        updateRecipientStatusPosition();
        notifyItemRangeRemoved(positionStart, itemCount);

        if (Log.isPerfLoggable()) {
            Log.perf("Messages adapter - onQueryItemRangeRemoved. Position start: " + positionStart
                    + " Count: " + itemCount);
        }
    }

    @Override
    public void onQueryItemMoved(RecyclerViewController controller, int fromPosition,
            int toPosition) {
        updateRecipientStatusPosition();
        notifyItemMoved(fromPosition, toPosition);

        if (Log.isPerfLoggable()) {
            Log.perf("Conversations adapter - onQueryItemMoved. From: " + fromPosition + " To: "
                    + toPosition);
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
