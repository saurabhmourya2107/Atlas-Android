package com.layer.ui.util.imagecache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.layer.ui.util.imagecache.requesthandlers.MessagePartRequestHandler;
import com.layer.ui.util.imagecache.transformations.CircleTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import static com.layer.ui.util.Log.TAG;

import java.util.HashSet;
import java.util.Set;

public class PicassoImageCacheWrapper implements ImageCacheWrapper {
    protected final static CircleTransform SINGLE_TRANSFORM = new CircleTransform(TAG + ".single");
    protected final static CircleTransform MULTI_TRANSFORM = new CircleTransform(TAG + ".multi");
    protected final Picasso mPicasso;
    private Set<Target> mTargets;

    public PicassoImageCacheWrapper(MessagePartRequestHandler messagePartRequestHandler, Context context) {
        mPicasso = new Picasso.Builder(context)
                .loggingEnabled(true)
                .addRequestHandler(messagePartRequestHandler)
                .build();
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