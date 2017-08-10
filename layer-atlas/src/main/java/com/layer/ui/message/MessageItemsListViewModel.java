package com.layer.ui.message;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.ui.adapters.ItemRecyclerViewAdapter;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.util.DateFormatter;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.views.SwipeableItem;

import java.util.List;

public class MessageItemsListViewModel extends BaseObservable {
    private ItemRecyclerViewAdapter mMessageItemsAdapter;
    private List<CellFactory> mCellFactories;
    private SwipeableItem.OnSwipeListener<Message> mOnMessageSwipeListener;

    public MessageItemsListViewModel(Context context, LayerClient layerClient,
            ImageCacheWrapper imageCacheWrapper, DateFormatter dateFormatter) {
        mMessageItemsAdapter = new MessagesAdapter(context, layerClient, imageCacheWrapper, dateFormatter);
    }

    @Bindable
    public ItemRecyclerViewAdapter getMessageItemsAdapter() {
        return mMessageItemsAdapter;
    }

    @Bindable
    public List<CellFactory> getCellFactories() {
        return mCellFactories;
    }

    public void setCellFactories(List<CellFactory> cellFactories) {
        mCellFactories = cellFactories;
    }

    public void setOnMessageSwipeListener(SwipeableItem.OnSwipeListener<Message> onMessageSwipeListener) {
        mOnMessageSwipeListener = onMessageSwipeListener;
    }

    public SwipeableItem.OnSwipeListener<Message> getOnMessageSwipeListener() {
        return mOnMessageSwipeListener;
    }
}
