package com.layer.ui.avatar;

import com.layer.sdk.messaging.Identity;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

public class AvatarViewModelImpl implements AvatarViewModel  {

    private IdentityNameFormatter mIdentityNameFormatter;
    private ImageCacheWrapper mImageCacheWrapper;

    public AvatarViewModelImpl(ImageCacheWrapper imageCacheWrapper) {
        mImageCacheWrapper = imageCacheWrapper;
    }

    public void setIdentityNameFormatter(IdentityNameFormatter identityNameFormatter) {
        mIdentityNameFormatter = identityNameFormatter;
    }

    public String getInitialsForAvatarView(Identity identity) {
        return mIdentityNameFormatter.getInitials(identity);
    }

    @Override
    public ImageCacheWrapper getImageCacheWrapper() {
        return mImageCacheWrapper;
    }

}
