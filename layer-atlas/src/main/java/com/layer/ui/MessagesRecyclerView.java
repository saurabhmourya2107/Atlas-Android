/*
 * Copyright (c) 2015 Layer. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.layer.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.View;

import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;
import com.layer.ui.adapters.ItemRecyclerViewAdapter;
import com.layer.ui.message.MessagesAdapter;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.message.messagetypes.MessageStyle;
import com.layer.ui.recyclerview.ItemsRecyclerView;
import com.layer.ui.util.itemanimators.NoChangeAnimator;
import com.layer.ui.util.views.SwipeableItem;

import java.util.List;

public class MessagesRecyclerView extends ItemsRecyclerView<Message> {
    private LinearLayoutManager mLayoutManager;
    private ItemTouchHelper mSwipeItemTouchHelper;
    private boolean mShouldShowAvatarsInOneOnOneConversations;

    private MessageStyle mMessageStyle;

    public MessagesRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseStyle(getContext(), attrs, defStyle);
        init();
    }

    public MessagesRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessagesRecyclerView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setStackFromEnd(true);
        setLayoutManager(mLayoutManager);
        // Don't flash items when changing content
        setItemAnimator(new NoChangeAnimator());
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throwExceptionIfNotMessageAdapter(adapter);
        setAdapter((ItemRecyclerViewAdapter) adapter);
    }

    private void throwExceptionIfNotMessageAdapter(Adapter adapter) {
        if (adapter == null || !(adapter instanceof MessagesAdapter)) {
            throw new IllegalArgumentException("The argument passed is not of the correct type");
        }
    }

    public void setAdapter(ItemRecyclerViewAdapter adapter) {
        throwExceptionIfNotMessageAdapter(adapter);

        super.setAdapter(adapter);
        mAdapter.setStyle(mMessageStyle);
        setShouldShowAvatarInOneOnOneConversations(mShouldShowAvatarsInOneOnOneConversations);
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                for (CellFactory factory : ((MessagesAdapter) mAdapter).getCellFactories()) {
                    factory.onScrollStateChanged(newState);
                }
            }
        });

        // Create an adapter that auto-scrolls if we're already at the bottom
        ((MessagesAdapter) mAdapter).setRecyclerView(this)
                .setOnMessageAppendListener(new MessagesAdapter.OnMessageAppendListener() {
                    @Override
                    public void onMessageAppend(MessagesAdapter adapter, Message message) {
                        autoScroll();
                    }
                });
    }

    /**
     * Updates the underlying MessagesAdapter with a Query for Messages in the given
     * Conversation.
     *
     * @param conversation Conversation to display Messages for.
     * @return This AtlasMessagesRecyclerView.
     */
    public void setConversation(Conversation conversation) {
        if (conversation != null) {
            ((MessagesAdapter) mAdapter).setReadReceiptsEnabled(conversation.isReadReceiptsEnabled());
        }
        ((MessagesAdapter) mAdapter).setQuery(Query.builder(Message.class)
                .predicate(new Predicate(Message.Property.CONVERSATION, Predicate.Operator.EQUAL_TO, conversation))
                .sortDescriptor(new SortDescriptor(Message.Property.POSITION, SortDescriptor.Order.ASCENDING))
                .build()).refresh();
    }

    public void setOnMessageSwipeListener(SwipeableItem.OnItemSwipeListener<Message> listener) {
        if (mSwipeItemTouchHelper != null) {
            mSwipeItemTouchHelper.attachToRecyclerView(null);
        }
        if (listener == null) {
            mSwipeItemTouchHelper = null;
        } else {
            listener.setAdapter(mAdapter);
            mSwipeItemTouchHelper = new ItemTouchHelper(listener);
            mSwipeItemTouchHelper.attachToRecyclerView(this);
        }
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#addCellFactories(List)
     */
    public void setCellFactories(List<CellFactory> cellFactories) {
        ((MessagesAdapter) mAdapter).addCellFactories(cellFactories);
    }

    public void setTextTypeface(Typeface myTypeface, Typeface otherTypeface) {
        mMessageStyle.setMyTextTypeface(myTypeface);
        mMessageStyle.setOtherTextTypeface(otherTypeface);
    }

    /**
     * Convenience pass-through to this list's LinearLayoutManager.
     *
     * @see LinearLayoutManager#findLastVisibleItemPosition()
     */
    private int findLastVisibleItemPosition() {
        return mLayoutManager.findLastVisibleItemPosition();
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#setFooterView(View)
     */
    public void setFooterView(View footerView) {
        ((MessagesAdapter) mAdapter).setFooterView(footerView);
        autoScroll();
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#getFooterView()
     */
    public View getFooterView() {
        return ((MessagesAdapter) mAdapter).getFooterView();
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#getShouldShowAvatarInOneOnOneConversations()
     */
    public boolean getShouldShowAvatarInOneOnOneConversations() {
        return ((MessagesAdapter) mAdapter).getShouldShowAvatarInOneOnOneConversations();
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#setShouldShowAvatarInOneOnOneConversations(boolean)
     */
    public void setShouldShowAvatarInOneOnOneConversations(boolean shouldShowAvatarInOneOnOneConversations) {
        ((MessagesAdapter) mAdapter).setShouldShowAvatarInOneOnOneConversations(shouldShowAvatarInOneOnOneConversations);
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#getShouldShowAvatarPresence()
     */
    public boolean getShouldShowAvatarPresence() {
        return ((MessagesAdapter) mAdapter).getShouldShowAvatarPresence();
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#setShouldShowAvatarPresence(boolean)
     */
    public void setShouldShowAvatarPresence(boolean shouldShowAvatarPresence) {
        ((MessagesAdapter) mAdapter).setShouldShowAvatarPresence(shouldShowAvatarPresence);
    }

    public void onDestroy() {
        if (mAdapter != null) {
            mAdapter.onDestroy();
        }
    }

    /**
     * Scrolls if the user is at the end
     */
    private void autoScroll() {
        int end = mAdapter.getItemCount() - 1;
        if (end <= 0) return;
        int visible = findLastVisibleItemPosition();
        // -3 because -1 seems too finicky
        if (visible >= (end - 3)) scrollToPosition(end);
    }

    public void parseStyle(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessagesRecyclerView, R.attr.MessagesRecyclerView, defStyle);
        MessageStyle.Builder messageStyleBuilder = new MessageStyle.Builder();
        messageStyleBuilder.myTextColor(ta.getColor(R.styleable.MessagesRecyclerView_myTextColor, context.getResources().getColor(R.color.layer_ui_text_black)));
        int myTextStyle = ta.getInt(R.styleable.MessagesRecyclerView_myTextStyle, Typeface.NORMAL);
        messageStyleBuilder.myTextStyle(myTextStyle);
        String myTextTypefaceName = ta.getString(R.styleable.MessagesRecyclerView_myTextTypeface);
        messageStyleBuilder.myTextTypeface(myTextTypefaceName != null ? Typeface.create(myTextTypefaceName, myTextStyle) : null);
        messageStyleBuilder.myTextSize(ta.getDimensionPixelSize(R.styleable.MessagesRecyclerView_myTextSize, context.getResources().getDimensionPixelSize(R.dimen.layer_ui_text_size_message_item)));

        messageStyleBuilder.otherTextColor(ta.getColor(R.styleable.MessagesRecyclerView_theirTextColor, context.getResources().getColor(R.color.layer_ui_color_primary_blue)));
        int otherTextStyle = ta.getInt(R.styleable.MessagesRecyclerView_theirTextStyle, Typeface.NORMAL);
        messageStyleBuilder.otherTextStyle(otherTextStyle);
        String otherTextTypefaceName = ta.getString(R.styleable.MessagesRecyclerView_theirTextTypeface);
        messageStyleBuilder.otherTextTypeface(otherTextTypefaceName != null ? Typeface.create(otherTextTypefaceName, otherTextStyle) : null);
        messageStyleBuilder.otherTextSize(ta.getDimensionPixelSize(R.styleable.MessagesRecyclerView_theirTextSize, context.getResources().getDimensionPixelSize(R.dimen.layer_ui_text_size_message_item)));

        messageStyleBuilder.myBubbleColor(ta.getColor(R.styleable.MessagesRecyclerView_myBubbleColor, context.getResources().getColor(R.color.layer_ui_color_primary_blue)));
        messageStyleBuilder.otherBubbleColor(ta.getColor(R.styleable.MessagesRecyclerView_theirBubbleColor, context.getResources().getColor(R.color.layer_ui_color_primary_gray)));

        mShouldShowAvatarsInOneOnOneConversations = ta.getBoolean(R.styleable.MessagesRecyclerView_shouldShowAvatarsInOneOnOneConversations, false);
        ta.recycle();
        this.mMessageStyle = messageStyleBuilder.build();
    }
}
