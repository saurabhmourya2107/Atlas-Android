package com.layer.ui.util.imagecache;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.UUID;

/**
 * This class wraps everything needed in a Bitmap
 * The Bitmap is used in {@link com.layer.ui.avatar.Avatar.ViewModel#fetchBitmap(BitmapWrapper)}
 * and also in {@link PicassoImageCacheWrapper}
 * Properties can be added to BitmapWrapper to suit other ImageCache library
 * We use Picasso in the implementation
 */
public class BitmapWrapper {

    private final UUID mUniqueId;
    private Bitmap mBitmap;
    private String mUrl;
    private int mWidth, mHeight;
    private boolean mIsMultiTransform;

    public BitmapWrapper(@NonNull String url, int width, int height, boolean isMultiTransform) {
        mUniqueId = UUID.randomUUID();
        mUrl = url;
        mWidth = width;
        mHeight = height;
        mIsMultiTransform = isMultiTransform;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public BitmapWrapper setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        return this;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getHeight() {
        return mHeight;
    }

    public UUID getUniqueId() {
        return mUniqueId;
    }

    public boolean hasMultiTransform() {
        return mIsMultiTransform;
    }

    public void setMultiTransform(boolean multiTransform) {
        mIsMultiTransform = multiTransform;
    }

}
