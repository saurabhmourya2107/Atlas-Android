package com.layer.ui.util.views;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.layer.sdk.query.Queryable;
import com.layer.ui.R;
import com.layer.ui.adapters.ItemRecyclerViewAdapter;
import com.layer.ui.adapters.ItemViewHolder;
import com.layer.ui.style.ItemStyle;
import com.layer.ui.viewmodel.ItemViewModel;


public class SwipeableItem extends FrameLayout {
    private static final int[] STATES_SWIPING_ACTIVE = {R.attr.state_swiping};

    private boolean mSwipingActive = false;

    public SwipeableItem(Context context) {
        super(context);
    }

    public SwipeableItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeableItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if (mSwipingActive) {
            final int[] drawableState = super.onCreateDrawableState(extraSpace + STATES_SWIPING_ACTIVE.length);
            mergeDrawableStates(drawableState, STATES_SWIPING_ACTIVE);
            return drawableState;
        } else {
            return super.onCreateDrawableState(extraSpace);
        }
    }

    public SwipeableItem setSwipingActive(boolean swipingActive) {
        if (mSwipingActive == swipingActive) return this;
        mSwipingActive = swipingActive;
        refreshDrawableState();
        return this;
    }


    /**
     * Listens for item swipes on an Item.
     */
    public static abstract class OnItemSwipeListener<ITEM extends Queryable> extends ItemTouchHelper.SimpleCallback {
        final float SWIPE_RATIO = 1f;
        final float DELETE_RATIO = 0.5f;
        final float MIN_ALPHA = 0f;
        final float MAX_ALPHA = 1f;
        private ItemRecyclerViewAdapter<ITEM, ItemViewModel<ITEM>, ViewDataBinding, ItemStyle,
                ItemViewHolder<ITEM, ItemViewModel<ITEM>, ViewDataBinding, ItemStyle>> mAdapter;

        public OnItemSwipeListener() {
            super(0, ItemTouchHelper.RIGHT);
        }

        @Override
        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return DELETE_RATIO;
        }

        /**
         * Alerts the listener to item swipes.
         *
         * @param item The item swiped.
         */
        public abstract void onSwipe(ITEM item, int direction);

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (mAdapter == null) return;
            ItemViewHolder<ITEM, ItemViewModel<ITEM>, ViewDataBinding, ItemStyle> vH = (ItemViewHolder) viewHolder;
            onSwipe(mAdapter.getItem(vH), direction);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            View swipeable = viewHolder.itemView.findViewById(R.id.swipeable);
            if (swipeable == null) {
                super.clearView(recyclerView, viewHolder);
                return;
            }
            swipeable.setTranslationX(0);
            ((SwipeableItem) viewHolder.itemView).setSwipingActive(false);
            View leavebehind = viewHolder.itemView.findViewById(R.id.leavebehind);
            if (leavebehind == null) return;
            leavebehind.setVisibility(GONE);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            float maxSwipeDelta = viewHolder.itemView.getWidth() * SWIPE_RATIO;
            float maxDeleteDelta = viewHolder.itemView.getWidth() * DELETE_RATIO;

            float swipeDelta = Math.min(Math.abs(dX), maxSwipeDelta);
            float deleteDelta = Math.min(Math.abs(dX), maxDeleteDelta);

            final float dir = Math.signum(dX);

            View swipeable = viewHolder.itemView.findViewById(R.id.swipeable);
            if (swipeable == null) {
                super.onChildDraw(c, recyclerView, viewHolder, dir * swipeDelta, dY, actionState, isCurrentlyActive);
                return;
            }

            // Backgrounds
            ((SwipeableItem) viewHolder.itemView).setSwipingActive(dir != 0 || isCurrentlyActive);

            if (dir == 0) {
                swipeable.setTranslationX(0);
            } else {
                swipeable.setTranslationX(dir * swipeDelta);
            }

            View leavebehind = viewHolder.itemView.findViewById(R.id.leavebehind);
            if (leavebehind == null) return;

            if (dir == 0) {
                leavebehind.setVisibility(GONE);
            } else {
                leavebehind.setVisibility(VISIBLE);
                float alpha = MIN_ALPHA + ((MAX_ALPHA - MIN_ALPHA) * deleteDelta / maxDeleteDelta);
                leavebehind.setAlpha(alpha);
            }
        }

        public void setAdapter(ItemRecyclerViewAdapter<ITEM, ItemViewModel<ITEM>, ViewDataBinding,
                ItemStyle, ItemViewHolder<ITEM, ItemViewModel<ITEM>, ViewDataBinding, ItemStyle>> adapter) {
            mAdapter = adapter;
        }
    }
}
