package com.layer.ui.adapters;

import android.databinding.ViewDataBinding;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.layer.sdk.query.Queryable;
import com.layer.ui.style.ItemStyle;
import com.layer.ui.viewmodel.ItemViewModel;

public class ItemViewHolder<ITEM extends Queryable, VIEW_MODEL extends ItemViewModel<ITEM>, BINDING extends ViewDataBinding, STYLE extends ItemStyle>
        extends RecyclerView.ViewHolder {

    protected VIEW_MODEL mViewModel;
    protected STYLE mStyle;
    protected BINDING mBinding;

    public ItemViewHolder(BINDING binding, VIEW_MODEL viewModel) {
        super(binding.getRoot());
        mBinding = binding;
        mViewModel = viewModel;
    }

    public BINDING getBinding() {
        return mBinding;
    }

    @CallSuper
    public void setItem(ITEM item) {
        mViewModel.setItem(item);

        getBinding().getRoot().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return getViewModel().getItemClickListener().onItemLongClick(getItem());
            }
        });
    }

    @CallSuper
    public void setEmpty() {

    }

    public ITEM getItem() {
        return mViewModel.getItem();
    }

    public STYLE getStyle() {
        return mStyle;
    }

    @CallSuper
    public void setStyle(STYLE style) {
        mStyle = style;
    }

    public VIEW_MODEL getViewModel() {
        return mViewModel;
    }
}
