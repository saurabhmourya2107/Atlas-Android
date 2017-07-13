package com.layer.ui.util.imagecache;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.layer.ui.util.Log;
import com.layer.ui.util.imagecache.transformations.CircleTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import static com.layer.ui.util.Log.TAG;
import static com.layer.ui.util.Log.VERBOSE;

import java.util.HashSet;
import java.util.Set;

public class PicassoImageCacheWrapper implements ImageCacheWrapper {
    protected final static CircleTransform SINGLE_TRANSFORM = new CircleTransform(TAG + ".single");
    protected final static CircleTransform MULTI_TRANSFORM = new CircleTransform(TAG + ".multi");
    protected final Picasso mPicasso;
    /*
        Picasso keeps a weak reference to the target when you load into a target,
        hence we need to keep a strong reference to the targets to prevent Garbage Collector from
        getting rid of the Targets.
     */
    private Set<Target> mTargets;

    public PicassoImageCacheWrapper(Picasso picasso) {
        mPicasso = picasso;
        mTargets = new HashSet<>();
    }


    @Override
    public void fetchBitmap(final BitmapWrapper bitmapWrapper, final Callback callback) {

        boolean isMultiTransform = bitmapWrapper.hasMultiTransform();
        Target target = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                bitmapWrapper.setBitmap(bitmap);
                callback.onSuccess();
                mTargets.remove(this);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (Log.isLoggable(VERBOSE)) {
                    Log.v("onBitMapFailed :" + errorDrawable);
                }

                bitmapWrapper.setBitmap(null);
                callback.onFailure();
                mTargets.remove(this);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };

        RequestCreator creator = mPicasso.load(bitmapWrapper.getUrl())
                .tag(bitmapWrapper.getUniqueId())
                .noPlaceholder()
                .noFade()
                .centerCrop()
                .resize(bitmapWrapper.getWidth(), bitmapWrapper.getHeight());

        mTargets.add(target);
        creator.transform(isMultiTransform ? MULTI_TRANSFORM : SINGLE_TRANSFORM)
                .into(target);
    }

    public void cancelBitmap(BitmapWrapper bitmapWrapper) {
        if (bitmapWrapper != null) {
            mPicasso.cancelTag(bitmapWrapper.getUniqueId());
        }
    }
}