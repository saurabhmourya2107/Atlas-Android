package com.layer.ui.avatar;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;

import com.layer.sdk.messaging.Identity;
import com.layer.ui.util.imagecache.BitmapWrapper;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

import java.lang.ref.WeakReference;


public class AvatarViewModel implements Avatar.ViewModel  {

    private IdentityNameFormatter mIdentityNameFormatter;
    private ImageCacheWrapper mImageCacheWrapper;
    private WeakReference<View> mView;

    public AvatarViewModel(ImageCacheWrapper imageCacheWrapper) {
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
