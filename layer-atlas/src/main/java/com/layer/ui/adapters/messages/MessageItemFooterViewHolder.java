package com.layer.ui.adapters.messages;

import android.databinding.ViewDataBinding;
import android.view.ViewGroup;

import com.layer.sdk.messaging.Message;
import com.layer.ui.adapters.ItemViewHolder;
import com.layer.ui.databinding.UiMessageItemFooterBinding;
import com.layer.ui.messageitem.MessageItemViewModel;
import com.layer.ui.messagetypes.MessageStyle;

public class MessageItemFooterViewHolder extends
        ItemViewHolder<Message, MessageItemViewModel, ViewDataBinding, MessageStyle> {

    protected ViewGroup mRoot;
    public MessageItemFooterViewHolder(UiMessageItemFooterBinding binding) {
        super(binding, null);
        mRoot = binding.swipeable;
    }
}
