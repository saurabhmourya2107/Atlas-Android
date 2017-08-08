package com.layer.ui.util.imagecache;

import android.net.Uri;


public class ImageRequestParameters {

    private final ImageCacheWrapper.Callback mCallBack;
    private Uri mUri;
    private String mTag;
    private int mPlaceholder;
    private boolean mShouldCenterImage;
    private int mResizeWidthTo;
    private int mResizeHeightTo;
    private boolean mShouldTransformIntoRound;
    private float mRotateAngleTo;
    private boolean mShouldScaleDownTo;

    public ImageRequestParameters(Builder builder) {
        mUri = builder.mUri;
        mPlaceholder = builder.mPlaceholder;
        mResizeWidthTo = builder.mResizeWidthTo;
        mResizeHeightTo = builder.mResizeHeightTo;
        mCallBack = builder.mCallBack;
        mTag = builder.mTag;
        mShouldCenterImage = builder.mShouldCenterImage;
        mShouldTransformIntoRound = builder.mShouldTransformIntoRound;
        mRotateAngleTo = builder.mRotateAngleTo;
        mShouldScaleDownTo = builder.mShouldScaleDownTo;
    }

    public Uri getUri() {
        return mUri;
    }

    public String getTag() {
        return mTag;
    }

    public int getPlaceholder() {
        return mPlaceholder;
    }

    public boolean isShouldCenterImage() {
        return mShouldCenterImage;
    }

    public int getResizeWidthTo() {
        return mResizeWidthTo;
    }

    public int getResizeHeightTo() {
        return mResizeHeightTo;
    }

    public boolean shouldTransformIntoRound() {
        return mShouldTransformIntoRound;
    }

    public ImageCacheWrapper.Callback getCallback() {
        return mCallBack;
    }

    public float getRotateAngleTo() {
        return mRotateAngleTo;
    }

    public boolean shouldScaleDownTo() {
        return mShouldScaleDownTo;
    }

    public static class Builder {
        private String mTag;
        private boolean mShouldCenterImage;
        private boolean mShouldTransformIntoRound;
        private float mRotateAngleTo;
        private boolean mShouldScaleDownTo;
        private Uri mUri;
        private int mPlaceholder;
        private int mResizeWidthTo;
        private int mResizeHeightTo;
        private ImageCacheWrapper.Callback mCallBack;

        public Builder(Uri uri, int placeholder, int resizeWidthTo, int resizeHeightTo, ImageCacheWrapper.Callback callback) {
            mUri = uri;
            mPlaceholder = placeholder;
            mResizeWidthTo = resizeWidthTo;
            mResizeHeightTo = resizeHeightTo;
            mCallBack = callback;
        }

        public Builder setTag(String tag) {
            mTag = tag;
            return this;
        }

        public Builder setShouldCenterImage(boolean shouldCenterImage) {
            mShouldCenterImage = shouldCenterImage;
            return this;
        }

        public Builder setShouldTransformIntoRound(boolean shouldTransformIntoRound) {
            mShouldTransformIntoRound = shouldTransformIntoRound;
            return this;
        }

        public Builder setRotateAngleTo(float rotateAngleTo) {
            mRotateAngleTo = rotateAngleTo;
            return this;

        }

        public Builder setShouldScaleDownTo(boolean shouldScaleDownTo) {
            mShouldScaleDownTo = shouldScaleDownTo;
            return this;
        }

        public ImageRequestParameters build() {
            return new ImageRequestParameters(this);
        }
    }
}
