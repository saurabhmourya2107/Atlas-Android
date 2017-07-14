package com.layer.ui.avatar;

import android.support.annotation.NonNull;
import android.view.View;

import com.layer.sdk.messaging.Identity;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

import java.lang.ref.WeakReference;


public class AvatarViewModelImpl implements AvatarViewModel  {

    private IdentityNameFormatter mIdentityNameFormatter;
    private ImageCacheWrapper mImageCacheWrapper;
    private WeakReference<View> mView;

    public AvatarViewModelImpl(ImageCacheWrapper imageCacheWrapper) {
        mImageCacheWrapper = imageCacheWrapper;
    }

    public void setIdentityNameFormatter(IdentityNameFormatter identityNameFormatter) {
        mIdentityNameFormatter = identityNameFormatter;
    }

    public String getInitialsForAvatarView(Identity added) {
        return mIdentityNameFormatter.getInitials(added);
    }

    @Override
    public void setView(@NonNull WeakReference<View> view) {
        mView = view;
    }

    @Override
    public ImageCacheWrapper getImageCacheWrapper() {
        return mImageCacheWrapper;
    }

}
