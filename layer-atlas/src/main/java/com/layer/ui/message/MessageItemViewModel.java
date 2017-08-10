package com.layer.ui.message;

import android.databinding.Bindable;
import android.view.View;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.ui.recyclerview.OnItemClickListener;
import com.layer.ui.viewmodel.ItemViewModel;

import java.util.Collections;
import java.util.Set;

public class MessageItemViewModel extends ItemViewModel<Message> {

    private boolean mIsOneOnOne;
    private boolean mShouldShowAvatar;
    private boolean mIsClusterSpaceVisible;
    private boolean mIsDisplayName;
    private boolean mIsBindDateTimeForMessage;
    private boolean mShouldClusterBeVisible;
    private String mTimeGroupDay;
    private boolean mIsMessageSent;
    private String mSender;
    private Set<Identity> mParticipants;
    private String mRecipientStatus;
    private String mGroupTime;
    private boolean mRecipientStatusVisible;
    private boolean mIsMyCellType;

    public MessageItemViewModel(
            OnItemClickListener<Message> itemClickListener) {
        super(itemClickListener);
    }


    public boolean isClusterSpaceVisible() {
        return mIsClusterSpaceVisible;
    }

    public int getAvatarVisibility() {
        if (mIsMyCellType) {
            return View.GONE;
        }

        if (isOneOnOne()) {
            if (isShouldShowAvatar()) {
                return View.VISIBLE;
            } else {
                return View.GONE;
            }
        } else if (mShouldClusterBeVisible) {
            return View.VISIBLE;
        }

        return View.INVISIBLE;
    }

    public void setShouldShowAvatar(
            boolean shouldShowAvatar) {
        mShouldShowAvatar = shouldShowAvatar;
    }

    public void setClusterSpaceVisible(boolean isClusterSpaceVisible) {
        mIsClusterSpaceVisible = isClusterSpaceVisible;
    }

    public void setOneOnOne(boolean oneOnOne) {
        mIsOneOnOne = oneOnOne;
    }

    private boolean isOneOnOne() {
        return mIsOneOnOne;
    }

    public void setShouldClusterBeVisible(boolean shouldClusterBeVisible) {
        mShouldClusterBeVisible = shouldClusterBeVisible;
    }

    private boolean isShouldShowAvatar() {
        return mShouldShowAvatar;
    }

    @Bindable
    public Set<Identity> getParticipants() {
        return mParticipants;
    }

    public void setParticipants(Identity identity) {
        mParticipants = Collections.singleton(identity);
    }

    @Bindable
    public String getTimeGroupDay() {
       return mTimeGroupDay;
    }

    public void setTimeGroupDay(String timeGroupDay) {
        mTimeGroupDay = timeGroupDay;
    }

    @Bindable
    public boolean isBindDateTimeForMessage() {
        return mIsBindDateTimeForMessage;
    }

    public void setIsBindDateTimeForMessage(boolean isBindDateTimeForMessage) {
        mIsBindDateTimeForMessage = isBindDateTimeForMessage;
    }

    @Bindable
    public String getRecipientStatus() {
        return mRecipientStatus;
    }

    public void setRecipientStatus(String recipientStatus) {
        mRecipientStatus = recipientStatus;
    }

    @Bindable
    public boolean isMessageSent() {
        return mIsMessageSent;
    }

    public void setMessageSent(boolean isMessageSent) {
        mIsMessageSent = isMessageSent;
    }

    @Bindable
    public String getSender() {
        return mSender;
    }

    public void setSender(String sender) {
        mSender = sender;
    }

    public void setIsDisplayName(boolean isDisplayName) {
        mIsDisplayName = isDisplayName;
    }

    @Bindable
    public boolean isDisplayName() {
        return mIsDisplayName;
    }

    @Bindable
    public String getGroupTime() {
        return mGroupTime;
    }

    public void setGroupTime(String groupTime) {
        mGroupTime = groupTime;
    }

    @Bindable
    public boolean isRecipientStatusVisible() {
        return mRecipientStatusVisible;
    }

    public void setRecipientStatusVisible(boolean recipientStatusVisible) {
        mRecipientStatusVisible = recipientStatusVisible;
    }

    public void setMyCellType(boolean isMyCellType) {
        mIsMyCellType = isMyCellType;
    }

    @Bindable
    public boolean isMyCellType() {
        return mIsMyCellType;
    }
}
