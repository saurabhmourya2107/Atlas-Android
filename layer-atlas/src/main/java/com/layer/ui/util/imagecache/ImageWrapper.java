package com.layer.ui.util.imagecache;

import android.net.Uri;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.widget.ImageView;

import java.lang.ref.WeakReference;


public class ImageWrapper implements ImageCacheWrapper.Callback {

    private Uri mUri;
    private String mTag;
    private int mPlaceholder;
    private boolean mShouldCenterImage;
    private int mResizeWidthTo;
    private int mResizeHeightTo;
    private boolean mShouldTransformIntoRound;
    private WeakReference<ImageView> mTargetView;
    private float mRotateAngleTo;
    private boolean mShouldScaleDownTo;
    private WeakReference<ContentLoadingProgressBar> mProgressBar;


    public ImageWrapper(Builder builder) {
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

    public WeakReference<ImageView> getTargetView() {
        return mTargetView;
    }

    public ImageCacheWrapper.Callback getCallback() {
        return this;
    }

    public float getRotateAngleTo() {
        return mRotateAngleTo;
    }

    public boolean shouldScaleDownTo() {
        return mShouldScaleDownTo;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public void setPlaceholder(int placeholder) {
        mPlaceholder = placeholder;
    }

    public void setResizeWidthTo(int resizeWidthTo) {
        mResizeWidthTo = resizeWidthTo;
    }

    public void setResizeHeightTo(int resizeHeightTo) {
        mResizeHeightTo = resizeHeightTo;
    }

    public void setTargetView(ImageView targetView) {
        mTargetView = new WeakReference<>(targetView);
    }

    @Override
    public void onSuccess() {
        hideProgressBar();
    }

    @Override
    public void onFailure() {
        hideProgressBar();
    }

    private void hideProgressBar() {
        ContentLoadingProgressBar contentLoadingProgressBar = mProgressBar.get();
        if (contentLoadingProgressBar != null) {
            contentLoadingProgressBar.hide();
        }
    }

    public void setProgressBar(ContentLoadingProgressBar progressBar) {
        mProgressBar = new WeakReference<>(progressBar);
    }

    public void setRotateAngleTo(float rotateAngleTo) {
        mRotateAngleTo = rotateAngleTo;
    }

    public static class Builder {
        private String mTag;
        private boolean mShouldCenterImage;
        private boolean mShouldTransformIntoRound;
        private float mRotateAngleTo;
        private boolean mShouldScaleDownTo;

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

        public ImageWrapper build() {
            return new ImageWrapper(this);
        }
    }
}
