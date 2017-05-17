package com.layer.ui.avatar;

import android.view.View;

import com.layer.sdk.messaging.Identity;
import com.layer.ui.util.imagecache.BitmapWrapper;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

import java.lang.ref.WeakReference;

/**
 * Avatar interface exposes the interaction between the AvatarView and AvatarViewModel Avatar.
 * @see Avatar.ViewModel is implemented by
 * @see AvatarViewModel and
 * @see AvatarView
 **/

public interface Avatar {

    /**
     * @see AvatarViewModel to view implementation
     */
    interface ViewModel {

        /**
         * Set the View on the ViewModel
         * @see AvatarView#init(ViewModel, IdentityNameFormatter)
         * @param view
         */
        void setView(WeakReference<View> view);

        /**
         * Fetch BitMap from ImageCacheWrapper Implentation passed into the ViewModel
         * @param bitmapWrapper
         */
        void fetchBitmap(BitmapWrapper bitmapWrapper);

        /**
         * Set Name Formatter for the Identity
         * @param identityNameFormatter
         */
        void setIdentityNameFormatter(IdentityNameFormatter identityNameFormatter);

        /**
         * getter for ImageCacherWrapper so that the view can cancel Bitmap Load request
         * @return
         */
        ImageCacheWrapper getImageCacheWrapper();

        /**
         * Returns the initial base on the
         * @see IdentityNameFormatter passed into the ViewModel
         * @param identity
         * @return
         */

        String getInitialsForAvatarView(Identity identity);
    }

}
