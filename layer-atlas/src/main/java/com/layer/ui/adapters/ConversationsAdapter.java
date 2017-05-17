package com.layer.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.layer.ui.avatar.AvatarView;
import com.layer.ui.R;
import com.layer.ui.avatar.AvatarViewModel;
import com.layer.ui.avatar.IdentityNameFormatterImpl;
import com.layer.ui.messagetypes.CellFactory;
import com.layer.ui.messagetypes.generic.GenericCellFactory;
import com.layer.ui.messagetypes.location.LocationCellFactory;
import com.layer.ui.messagetypes.singlepartimage.SinglePartImageCellFactory;
import com.layer.ui.messagetypes.text.TextCellFactory;
import com.layer.ui.messagetypes.threepartimage.ThreePartImageCellFactory;
import com.layer.ui.util.ConversationFormatter;
import com.layer.ui.util.ConversationStyle;
import com.layer.ui.util.IdentityRecyclerViewEventListener;
import com.layer.ui.util.Log;
import com.layer.ui.util.Util;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.RecyclerViewController;
import com.layer.sdk.query.SortDescriptor;
import com.layer.ui.conversationitem.ConversationItemViewModel;
import com.layer.ui.conversationitem.OnConversationItemClickListener;
import com.layer.ui.databinding.UiConversationItemBinding;
import com.layer.ui.messagetypes.CellFactory;
import com.layer.ui.conversationitem.ConversationItemFormatter;
import com.layer.ui.util.ConversationStyle;
import com.layer.ui.util.IdentityRecyclerViewEventListener;
import com.layer.ui.util.Log;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.squareup.picasso.Picasso;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> implements RecyclerViewController.Callback, BaseAdapter<Conversation> {
    protected final LayerClient mLayerClient;
    protected final Picasso mPicasso;
    private final RecyclerViewController<Conversation> mQueryController;
    private final LayoutInflater mInflater;
    private long mInitialHistory = 0;

    private OnConversationItemClickListener mConversationClickListener;
    private ConversationStyle mConversationStyle;
    private final IdentityRecyclerViewEventListener mIdentityEventListener;
    protected Set<CellFactory> mCellFactories;
    protected ConversationItemFormatter mConversationItemFormatter;

    public ConversationsAdapter(Context context, LayerClient client, Picasso picasso, ConversationItemFormatter conversationItemFormatter) {
        this(context, client, picasso, null, conversationItemFormatter);
    }

    public ConversationsAdapter(Context context, LayerClient client, Picasso picasso, Collection<String> updateAttributes, ConversationItemFormatter conversationItemFormatter) {
        mConversationItemFormatter = conversationItemFormatter;
    protected ConversationFormatter mConversationFormatter;
    private ImageCacheWrapper mImageCacheWrapper;

    public ConversationsAdapter(Context context, LayerClient client, Picasso picasso, ConversationFormatter conversationFormatter, ImageCacheWrapper imageCacheWrapper) {
        this(context, client, picasso, null, conversationFormatter, imageCacheWrapper);
    }

    public ConversationsAdapter(Context context, LayerClient client, Picasso picasso, Collection<String> updateAttributes, ConversationFormatter conversationFormatter, ImageCacheWrapper imageCacheWrapper) {
        mConversationFormatter = conversationFormatter;
        mImageCacheWrapper = imageCacheWrapper;
        Query<Conversation> query = Query.builder(Conversation.class)
                /* Only show conversations we're still a member of */
                .predicate(new Predicate(Conversation.Property.PARTICIPANT_COUNT, Predicate.Operator.GREATER_THAN, 1))

                /* Sort by the last Message's receivedAt time */
                .sortDescriptor(new SortDescriptor(Conversation.Property.LAST_MESSAGE_RECEIVED_AT, SortDescriptor.Order.DESCENDING))
                .build();
        mQueryController = client.newRecyclerViewController(query, updateAttributes, this);
        mLayerClient = client;
        mPicasso = picasso;
        mInflater = LayoutInflater.from(context);

        setHasStableIds(false);

        mIdentityEventListener = new IdentityRecyclerViewEventListener(this);
        mLayerClient.registerEventListener(mIdentityEventListener);
    }

    public ConversationsAdapter setCellFactories(CellFactory... cellFactories) {
        if (mCellFactories == null) {
            mCellFactories = new LinkedHashSet<CellFactory>();
        }
        Collections.addAll(mCellFactories, cellFactories);
        return this;
    }

    public ConversationsAdapter setCellFactories(Set<CellFactory> cellFactories) {
        mCellFactories = cellFactories;
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

    //==============================================================================================
    // Initial message history
    //==============================================================================================

    public ConversationsAdapter setInitialHistoricMessagesToFetch(long initialHistory) {
        mInitialHistory = initialHistory;
        return this;
    }

    public void setStyle(ConversationStyle conversationStyle) {
        this.mConversationStyle = conversationStyle;
    }

    private void syncInitialMessages(final int start, final int length) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long desiredHistory = mInitialHistory;
                if (desiredHistory <= 0) return;
                for (int i = start; i < start + length; i++) {
                    try {
                        final Conversation conversation = getItem(i);
                        if (conversation == null || conversation.getHistoricSyncStatus() != Conversation.HistoricSyncStatus.MORE_AVAILABLE) {
                            continue;
                        }
                        Query<Message> localCountQuery = Query.builder(Message.class)
                                .predicate(new Predicate(Message.Property.CONVERSATION, Predicate.Operator.EQUAL_TO, conversation))
                                .build();
                        long delta = desiredHistory - mLayerClient.executeQueryForCount(localCountQuery);
                        if (delta > 0) conversation.syncMoreHistoricMessages((int) delta);
                    } catch (IndexOutOfBoundsException e) {
                        // Concurrent modification
                    }
                }
            }
        }).start();
    }


    //==============================================================================================
    // Listeners
    //==============================================================================================

    public ConversationsAdapter setOnConversationClickListener(OnConversationItemClickListener conversationClickListener) {
        mConversationClickListener = conversationClickListener;
        return this;
    }


    //==============================================================================================
    // Adapter
    //==============================================================================================

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        UiConversationItemBinding binding = UiConversationItemBinding.inflate(mInflater, parent, false);
        binding.avatar.init(new AvatarViewModel(mImageCacheWrapper), new IdentityNameFormatterImpl());

        ConversationItemViewModel viewModel = new ConversationItemViewModel(mConversationItemFormatter, mConversationClickListener);
        return new ViewHolder(binding, viewModel, mConversationStyle);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        mQueryController.updateBoundPosition(position);
        viewHolder.bind(mQueryController.getItem(position), mLayerClient.getAuthenticatedUser());
    }

    @Override
    public int getItemCount() {
        return mQueryController.getItemCount();
    }

    @Override
    public Integer getPosition(Conversation conversation) {
        return mQueryController.getPosition(conversation);
    }

    @Override
    public Integer getPosition(Conversation conversation, int lastPosition) {
        return mQueryController.getPosition(conversation, lastPosition);
    }

    @Override
    public Conversation getItem(int position) {
        return mQueryController.getItem(position);
    }

    @Override
    public Conversation getItem(RecyclerView.ViewHolder viewHolder) {
        return ((ViewHolder) viewHolder).mConversationItemBinding.getViewModel().getConversation();
    }

    //==============================================================================================
    // UI update callbacks
    //==============================================================================================

    @Override
    public void onQueryDataSetChanged(RecyclerViewController controller) {
        syncInitialMessages(0, getItemCount());
        notifyDataSetChanged();

        if (Log.isPerfLoggable()) {
            Log.perf("Conversations adapter - onQueryDataSetChanged");
        }
    }

    @Override
    public void onQueryItemChanged(RecyclerViewController controller, int position) {
        notifyItemChanged(position);

        if (Log.isPerfLoggable()) {
            Log.perf("Conversations adapter - onQueryItemChanged. Position: " + position);
        }
    }

    @Override
    public void onQueryItemRangeChanged(RecyclerViewController controller, int positionStart, int itemCount) {
        notifyItemRangeChanged(positionStart, itemCount);

        if (Log.isPerfLoggable()) {
            Log.perf("Conversations adapter - onQueryItemRangeChanged. Position start: " + positionStart + " Count: " + itemCount);
        }
    }

    @Override
    public void onQueryItemInserted(RecyclerViewController controller, int position) {
        syncInitialMessages(position, 1);
        notifyItemInserted(position);

        if (Log.isPerfLoggable()) {
            Log.perf("Conversations adapter - onQueryItemInserted. Position: " + position);
        }
    }

    @Override
    public void onQueryItemRangeInserted(RecyclerViewController controller, int positionStart, int itemCount) {
        syncInitialMessages(positionStart, itemCount);
        notifyItemRangeInserted(positionStart, itemCount);

        if (Log.isPerfLoggable()) {
            Log.perf("Conversations adapter - onQueryItemRangeInserted. Position start: " + positionStart + " Count: " + itemCount);
        }
    }

    @Override
    public void onQueryItemRemoved(RecyclerViewController controller, int position) {
        notifyItemRemoved(position);

        if (Log.isPerfLoggable()) {
            Log.perf("Conversations adapter - onQueryItemRemoved. Position: " + position);
        }
    }

    @Override
    public void onQueryItemRangeRemoved(RecyclerViewController controller, int positionStart, int itemCount) {
        notifyItemRangeRemoved(positionStart, itemCount);

        if (Log.isPerfLoggable()) {
            Log.perf("Conversations adapter - onQueryItemRangeRemoved. Position start: " + positionStart + " Count: " + itemCount);
        }
    }

    @Override
    public void onQueryItemMoved(RecyclerViewController controller, int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);

        if (Log.isPerfLoggable()) {
            Log.perf("Conversations adapter - onQueryItemMoved. From: " + fromPosition + " To: " + toPosition);
        }
    }


    //==============================================================================================
    // Inner classes
    //==============================================================================================

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final UiConversationItemBinding mConversationItemBinding;
        private final ConversationStyle mConversationStyle;
        private ConversationItemViewModel mViewModel;

        public ViewHolder(UiConversationItemBinding binding, ConversationItemViewModel viewModel, ConversationStyle conversationStyle) {
            super(binding.getRoot());
            mConversationItemBinding = binding;
            mViewModel = viewModel;
            mConversationStyle = conversationStyle;
        }

        public void bind(final Conversation conversation, Identity authenticatedUser) {
            mViewModel.setConversation(conversation, authenticatedUser);
            mConversationItemBinding.setViewModel(mViewModel);
            mConversationItemBinding.setStyle(mConversationStyle);

            mConversationItemBinding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mViewModel.getOnConversationItemClickListener() != null) {
                        return mViewModel
                                .getOnConversationItemClickListener()
                                .onConversationLongClick(conversation);
                    }

                    return false;
                }
            });


            mConversationItemBinding.executePendingBindings();
        }
    }
}
