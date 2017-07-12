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
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.View;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.ui.adapters.ConversationsAdapter;
import com.layer.ui.conversationitem.OnConversationItemClickListener;
import com.layer.ui.messagetypes.CellFactory;
import com.layer.ui.messagetypes.location.LocationCellFactory;
import com.layer.ui.messagetypes.singlepartimage.SinglePartImageCellFactory;
import com.layer.ui.messagetypes.text.TextCellFactory;
import com.layer.ui.messagetypes.threepartimage.ThreePartImageCellFactory;
import com.layer.ui.util.AvatarStyle;
import com.layer.ui.conversationitem.ConversationItemFormatter;
import com.layer.ui.util.ConversationStyle;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.itemanimators.NoChangeAnimator;
import com.layer.ui.util.views.SwipeableItem;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConversationsRecyclerView extends RecyclerView {
    ConversationsAdapter mAdapter;
    private ItemTouchHelper mSwipeItemTouchHelper;
    private ConversationStyle conversationStyle;
    private Set<CellFactory> mDefaultCellFactories;
    private LayerClient mLayerClient;
    private Picasso mPicasso;

    public ConversationsRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseStyle(context, attrs, defStyle);
    }

    public ConversationsRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConversationsRecyclerView(Context context) {
        super(context);
    }

    public ConversationsRecyclerView init(LayerClient layerClient, Picasso picasso, ConversationItemFormatter conversationFormatter, ImageCacheWrapper imageCacheWrapper) {
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        manager.setStackFromEnd(false);
        setLayoutManager(manager);

        // Don't flash items when changing content
        setItemAnimator(new NoChangeAnimator());

        mLayerClient = layerClient;
        mPicasso = picasso;
        mAdapter = new ConversationsAdapter(getContext(), layerClient, conversationFormatter, imageCacheWrapper);
        mAdapter.setStyle(conversationStyle);
        super.setAdapter(mAdapter);
        refresh();

        return this;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throw new RuntimeException("ConversationsRecyclerView sets its own Adapter");
    }

    public ConversationsRecyclerView addCellFactories(CellFactory... cellFactories) {
        mAdapter.setCellFactories(cellFactories);
        return this;
    }

    public ConversationsRecyclerView addCellFactories(Set<CellFactory> cellFactories) {
        mAdapter.setCellFactories(cellFactories);
        return this;
    }

    /**
     * Performs cleanup when the Activity/Fragment using the adapter is destroyed.
     */
    public void onDestroy() {
        if (mAdapter != null) {
            mAdapter.onDestroy();
        }
    }

    /**
     * Automatically refresh on resume
     */
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE) return;
        refresh();
    }

    public ConversationsRecyclerView refresh() {
        if (mAdapter != null) mAdapter.refresh();
        return this;
    }

    /**
     * Convenience pass-through to this list's ConversationsAdapter.
     *
     * @see ConversationsAdapter#setOnConversationClickListener(OnConversationItemClickListener)
     */
    public ConversationsRecyclerView setOnConversationClickListener(OnConversationItemClickListener listener) {
        mAdapter.setOnConversationClickListener(listener);
        return this;
    }

    public ConversationsRecyclerView setOnConversationSwipeListener(SwipeableItem.OnSwipeListener<Conversation> listener) {
        if (mSwipeItemTouchHelper != null) {
            mSwipeItemTouchHelper.attachToRecyclerView(null);
        }
        if (listener == null) {
            mSwipeItemTouchHelper = null;
        } else {
            listener.setBaseAdapter((ConversationsAdapter) getAdapter());
            mSwipeItemTouchHelper = new ItemTouchHelper(listener);
            mSwipeItemTouchHelper.attachToRecyclerView(this);
        }
        return this;
    }

    private Set<CellFactory> getDefaultCellFactories() {
        if (mDefaultCellFactories == null) {
            mDefaultCellFactories = new LinkedHashSet<>();
        }
        if (mDefaultCellFactories.isEmpty()) {
            mDefaultCellFactories.addAll(Arrays.asList(new TextCellFactory(),
                    new ThreePartImageCellFactory(mLayerClient, mPicasso),
                    new LocationCellFactory(mPicasso),
                    new SinglePartImageCellFactory(mLayerClient, mPicasso)));
        }

        return mDefaultCellFactories;
    }

    /**
     * Convenience pass-through to this list's ConversationsAdapter.
     *
     * @see ConversationsAdapter#setInitialHistoricMessagesToFetch(long)
     */
    public ConversationsRecyclerView setInitialHistoricMessagesToFetch(long count) {
        mAdapter.setInitialHistoricMessagesToFetch(count);
        return this;
    }

    public ConversationsRecyclerView setTypeface(Typeface titleTypeface,
                                                 Typeface titleUnreadTypeface,
                                                 Typeface subtitleTypeface,
                                                 Typeface subtitleUnreadTypeface,
                                                 Typeface rightAcccessoryTextTypeface,
                                                 Typeface rightAccessoryUnreadTextTypeface) {
        conversationStyle.setTitleTextTypeface(titleTypeface);
        conversationStyle.setTitleUnreadTextTypeface(titleUnreadTypeface);
        conversationStyle.setSubtitleTextTypeface(subtitleTypeface);
        conversationStyle.setSubtitleUnreadTextTypeface(subtitleUnreadTypeface);
        conversationStyle.setRightAccessoryTextTypeface(rightAcccessoryTextTypeface);
        conversationStyle.setRightAccessoryUnreadTextTypeface(rightAccessoryUnreadTextTypeface);
        return this;
    }

    private void parseStyle(Context context, AttributeSet attrs, int defStyle) {
        ConversationStyle.Builder styleBuilder = new ConversationStyle.Builder();
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ConversationsRecyclerView, R.attr.ConversationsRecyclerView, defStyle);
        styleBuilder.titleTextColor(ta.getColor(R.styleable.ConversationsRecyclerView_cellTitleTextColor, context.getResources().getColor(R.color.layer_ui_text_gray)));
        int titleTextStyle = ta.getInt(R.styleable.ConversationsRecyclerView_cellTitleTextStyle, Typeface.NORMAL);
        styleBuilder.titleTextStyle(titleTextStyle);
        String titleTextTypefaceName = ta.getString(R.styleable.ConversationsRecyclerView_cellTitleTextTypeface);
        styleBuilder.titleTextTypeface(titleTextTypefaceName != null ? Typeface.create(titleTextTypefaceName, titleTextStyle) : null);

        styleBuilder.titleUnreadTextColor(ta.getColor(R.styleable.ConversationsRecyclerView_cellTitleUnreadTextColor, context.getResources().getColor(R.color.layer_ui_text_black)));
        int titleUnreadTextStyle = ta.getInt(R.styleable.ConversationsRecyclerView_cellTitleUnreadTextStyle, Typeface.BOLD);
        styleBuilder.titleUnreadTextStyle(titleUnreadTextStyle);
        String titleUnreadTextTypefaceName = ta.getString(R.styleable.ConversationsRecyclerView_cellTitleUnreadTextTypeface);
        styleBuilder.titleUnreadTextTypeface(titleUnreadTextTypefaceName != null ? Typeface.create(titleUnreadTextTypefaceName, titleUnreadTextStyle) : null);

        styleBuilder.subtitleTextColor(ta.getColor(R.styleable.ConversationsRecyclerView_cellSubtitleTextColor, context.getResources().getColor(R.color.layer_ui_text_gray)));
        int subtitleTextStyle = ta.getInt(R.styleable.ConversationsRecyclerView_cellSubtitleTextStyle, Typeface.NORMAL);
        styleBuilder.subtitleTextStyle(subtitleTextStyle);
        String subtitleTextTypefaceName = ta.getString(R.styleable.ConversationsRecyclerView_cellSubtitleTextTypeface);
        styleBuilder.subtitleTextTypeface(subtitleTextTypefaceName != null ? Typeface.create(subtitleTextTypefaceName, subtitleTextStyle) : null);

        styleBuilder.subtitleUnreadTextColor(ta.getColor(R.styleable.ConversationsRecyclerView_cellSubtitleUnreadTextColor, context.getResources().getColor(R.color.layer_ui_text_black)));
        int subtitleUnreadTextStyle = ta.getInt(R.styleable.ConversationsRecyclerView_cellSubtitleUnreadTextStyle, Typeface.NORMAL);
        styleBuilder.subtitleUnreadTextStyle(subtitleUnreadTextStyle);
        String subtitleUnreadTextTypefaceName = ta.getString(R.styleable.ConversationsRecyclerView_cellSubtitleUnreadTextTypeface);
        styleBuilder.subtitleUnreadTextTypeface(subtitleUnreadTextTypefaceName != null ? Typeface.create(subtitleUnreadTextTypefaceName, subtitleUnreadTextStyle) : null);

        styleBuilder.cellBackgroundColor(ta.getColor(R.styleable.ConversationsRecyclerView_cellBackgroundColor, Color.TRANSPARENT));
        styleBuilder.cellUnreadBackgroundColor(ta.getColor(R.styleable.ConversationsRecyclerView_cellUnreadBackgroundColor, Color.TRANSPARENT));

        styleBuilder.rightAccessoryTextColor(ta.getColor(R.styleable.ConversationsRecyclerView_rightAccessoryTextColor, context.getResources().getColor(R.color.layer_ui_color_primary_blue)));
        styleBuilder.rightAccessoryUnreadTextColor(ta.getColor(R.styleable.ConversationsRecyclerView_rightAccessoryUnreadTextColor, context.getResources().getColor(R.color.layer_ui_color_primary_blue)));

        AvatarStyle.Builder avatarStyleBuilder = new AvatarStyle.Builder();
        avatarStyleBuilder.avatarTextColor(ta.getColor(R.styleable.ConversationsRecyclerView_avatarTextColor, context.getResources().getColor(R.color.layer_ui_avatar_text)));
        avatarStyleBuilder.avatarBackgroundColor(ta.getColor(R.styleable.ConversationsRecyclerView_avatarBackgroundColor, context.getResources().getColor(R.color.layer_ui_avatar_background)));
        avatarStyleBuilder.avatarBorderColor(ta.getColor(R.styleable.ConversationsRecyclerView_avatarBorderColor, context.getResources().getColor(R.color.layer_ui_avatar_border)));
        styleBuilder.avatarStyle(avatarStyleBuilder.build());
        ta.recycle();
        conversationStyle = styleBuilder.build();
    }
}
