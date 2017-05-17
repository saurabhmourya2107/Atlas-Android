package com.layer.ui.util.imagecache;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.UUID;

public class BitmapWrapper {

    private final String mUniqueId;
    private Bitmap mBitmap;
    private String mUrl;
    private int mWidth, mHeight;
    private boolean mIsMultiTransform;

    public BitmapWrapper(@NonNull String url, int width, int height, boolean isMultiTransform) {
        mUniqueId = UUID.randomUUID().toString();
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

    public String getUniqueId() {
        return mUniqueId;
    }

    public boolean hasMultiTransform() {
        return mIsMultiTransform;
    }

    public void setMultiTransform(boolean multiTransform) {
        mIsMultiTransform = multiTransform;
    }

}
