package com.layer.ui.identity;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.query.Query;
import com.layer.ui.recyclerview.OnItemClickListener;
import com.layer.ui.util.DateFormatter;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

import java.util.Collection;
import java.util.Set;

public class IdentityItemsListViewModel extends BaseObservable {

    protected IdentityItemsAdapter mItemsAdapter;

    public IdentityItemsListViewModel(Context context, LayerClient layerClient,
                                      ImageCacheWrapper imageCacheWrapper,
                                      DateFormatter dateFormatter,
                                      IdentityFormatter identityFormatter) {
        mItemsAdapter = new IdentityItemsAdapter(context, layerClient, imageCacheWrapper,
                identityFormatter, dateFormatter);
    }

    public void setIdentities(Set<Identity> identities) {
        mItemsAdapter.setItems(identities);
        notifyChange();
    }

    public void setQuery(Query<Identity> query) {
        this.setQuery(query, null);
    }

    public void setQuery(Query<Identity> query, Collection<String> updateAttributes) {
        mItemsAdapter.setQuery(query, updateAttributes);
    }

    public void setItemClickListener(OnItemClickListener<Identity> itemClickListener) {
        mItemsAdapter.setItemClickListener(itemClickListener);
        notifyChange();
    }

    @Bindable
    public IdentityItemsAdapter getAdapter() {
        return mItemsAdapter;
    }
}
