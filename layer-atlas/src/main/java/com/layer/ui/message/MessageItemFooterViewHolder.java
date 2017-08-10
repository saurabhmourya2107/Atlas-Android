package com.layer.ui.message;

import android.databinding.ViewDataBinding;
import android.view.ViewGroup;

import com.layer.sdk.messaging.Message;
import com.layer.ui.adapters.ItemViewHolder;
import com.layer.ui.databinding.UiMessageItemFooterBinding;
import com.layer.ui.message.messagetypes.MessageStyle;

public class MessageItemFooterViewHolder extends
        ItemViewHolder<Message, MessageItemViewModel, ViewDataBinding, MessageStyle> {

    protected ViewGroup mRoot;
    public MessageItemFooterViewHolder(UiMessageItemFooterBinding binding) {
        super(binding, null);
        mRoot = binding.swipeable;
    }
}
