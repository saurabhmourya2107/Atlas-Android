package com.layer.ui.messagetypes.text;

import android.databinding.BaseObservable;
import android.graphics.Typeface;

import com.layer.ui.messagetypes.MessageStyle;

public class TextCellFactoryViewModel extends BaseObservable {
    private MessageStyle mMessageStyle;
    private boolean mIsMe;

    public TextCellFactoryViewModel(MessageStyle messageStyle, boolean isMe) {
        mMessageStyle = messageStyle;
        mIsMe = isMe;
    }

    public float getTextSize() {
        return mIsMe ? mMessageStyle.getMyTextSize() : mMessageStyle.getOtherTextSize();
    }

    public int getTextColor() {
        return mIsMe ? mMessageStyle.getMyTextColor() : mMessageStyle.getOtherTextColor();
    }

    public int getLinkTextColor() {
        return mIsMe ? mMessageStyle.getMyTextColor() : mMessageStyle.getOtherTextColor();
    }

    public Typeface getTypeFace() {
        return mIsMe ? mMessageStyle.getMyTextTypeface() : mMessageStyle.getOtherTextTypeface();
    }

    public int getMessageStyle() {
        return mIsMe ? mMessageStyle.getMyTextStyle() : mMessageStyle.getOtherTextStyle();
    }
}
