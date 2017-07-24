package com.layer.ui.messageitem;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.FrameLayout;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.ui.R;
import com.layer.ui.util.Util;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class MessageItemViewModel {
    private final LayerClient mLayerClient;
    private Message mMessage;
    private Context mContext;
    private final DateFormat mTimeFormat;
    private Integer mRecipientStatusPosition;
    private int mPosition;
    private int mReadCount = 0;
    private Map<Identity, Message.RecipientStatus> mStatuses;
    private boolean mIsOneOnOne;
    private boolean misReadReceiptsEnabled;
    private boolean mShouldShowAvatarInOneOnOneConversations;
    private boolean mIsCellTypeMe;
    private boolean mHasDelivered;
    private boolean mIsClusterSpaceVisible;
    private boolean mIsTimeGroupVisible;
    private boolean mIsDisplayName;
    private boolean mIsBindDateTimeForMessage;
    private boolean mShouldClusterBeVisible;

    public MessageItemViewModel(Builder builder) {
        mMessage = builder.mMessage;
        mIsOneOnOne = builder.mIsOneOnOne;
        mLayerClient = builder.mLayerClient;
        mPosition = builder.mPosition;
        mContext = builder.mContext;
        mIsCellTypeMe = builder.mIsCellTypeMe;
        mRecipientStatusPosition = builder.mIsRecipientStatusPosition;
        misReadReceiptsEnabled = builder.mIsReadReceiptsEnabled;
        mStatuses = mMessage.getRecipientStatus();
        mIsClusterSpaceVisible = builder.mIsClusterSpaceVisible;
        mIsTimeGroupVisible = builder.mIsTimeGroupVisible;
        mIsDisplayName = builder.mIsDisplayName;
        mShouldClusterBeVisible = builder.mShouldClusterBeVisible;
        mIsBindDateTimeForMessage = builder.mIsBindDateTimeForMessage;
        mShouldShowAvatarInOneOnOneConversations = builder.mShouldShowAvatarInOneOnOneConversations;
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(builder.mContext);
        updateValuesForRecipient();
    }

    private void updateValuesForRecipient() {
        if (mIsCellTypeMe && (misReadReceiptsEnabled && mRecipientStatusPosition != null
                && mRecipientStatusPosition == mPosition)) {

            mHasDelivered = false;
            for (Map.Entry<Identity, Message.RecipientStatus> entry : mStatuses.entrySet()) {
                // Only show receipts for other members
                if (entry.getKey().equals(mLayerClient.getAuthenticatedUser())) continue;
                // Skip receipts for members no longer in the conversation
                if (entry.getValue() == null) continue;

                switch (entry.getValue()) {
                    case READ:
                        mReadCount++;
                        break;
                    case DELIVERED:
                        mHasDelivered = true;
                        break;
                }
            }
        }
    }

    public boolean isClusterSpaceVisible() {
        return mIsClusterSpaceVisible;
    }

    public boolean isTimeGroupVisible() {
        return mIsTimeGroupVisible;
    }

    public boolean isMessageSent() {
        return mMessage.isSent();
    }

    public String getSender() {
        Identity identity = mMessage.getSender();
        return identity == null ? mContext.getString(R.string.layer_ui_message_item_unknown_user) : Util.getDisplayName(mMessage.getSender());
    }

    public boolean isDisplayName() {
        return mIsDisplayName;
    }

    @BindingAdapter("android:visibility")
    public static void isDisplayAvatar(FrameLayout frameLayout, AvatarViewDisplayWrapper avatarViewDisplayWrapper) {
        if (avatarViewDisplayWrapper.mOneOnOne) {
            if (avatarViewDisplayWrapper.mShouldShowAvatarInOneOnOneConversations) {
                frameLayout.setVisibility(View.VISIBLE);
            } else {
                frameLayout.setVisibility(View.GONE);
            }
        } else if (avatarViewDisplayWrapper.mShouldMClusterBeDisplayed) {
            frameLayout.setVisibility(View.VISIBLE);
        } else {
            frameLayout.setVisibility(View.INVISIBLE);
        }
    }

    public AvatarViewDisplayWrapper getAvatarDisplayWrapper() {
        return new AvatarViewDisplayWrapper(mIsOneOnOne, mShouldShowAvatarInOneOnOneConversations,
                mShouldClusterBeVisible);
    }

    public Set<Identity> getParticipants() {
        return Collections.singleton(mMessage.getSender());
    }

    public String getTimeGroupDay() {
        Date receivedAt = mMessage.getReceivedAt();
        if (receivedAt == null) receivedAt = new Date();
        return Util.formatTimeDay(mContext, receivedAt);
    }

    public String getGroupTime() {
        return " " + mTimeFormat.format(mMessage.getReceivedAt().getTime());
    }

    public boolean isBindDateTimeForMessage() {
        return mIsBindDateTimeForMessage;
    }

    public boolean isRecipientStatusVisible() {
        return mIsCellTypeMe && (misReadReceiptsEnabled && mRecipientStatusPosition != null
                && mRecipientStatusPosition == mPosition);
    }

    public String getRecipientStatus() {
        if (mIsCellTypeMe && (misReadReceiptsEnabled && mRecipientStatusPosition != null
                && mRecipientStatusPosition == mPosition)) {
            if (mReadCount > 0) {
                // Use 2 to include one other participant plus the current user
                if (mStatuses.size() > 2) {
                    return mContext.getResources()
                            .getQuantityString(R.plurals.layer_ui_message_item_read_muliple_participants,
                                    mReadCount, mReadCount);
                } else {
                    return mContext.getString(R.string.layer_ui_message_item_read);
                }
            } else if (mHasDelivered) {
                return mContext.getString(R.string.layer_ui_message_item_delivered);
            }
        }
        return "";
    }

    public static class Builder {
        private final LayerClient mLayerClient;
        private Message mMessage;
        private Context mContext;
        private Integer mIsRecipientStatusPosition;
        private int mPosition;
        private boolean mIsOneOnOne;
        private boolean mShouldShowAvatarInOneOnOneConversations;
        private boolean mIsReadReceiptsEnabled;
        private boolean mIsCellTypeMe;
        private boolean mIsClusterSpaceVisible;
        private boolean mIsTimeGroupVisible;
        private boolean mIsDisplayName;
        private boolean mIsBindDateTimeForMessage;
        private boolean mShouldClusterBeVisible;

        public Builder(LayerClient layerClient) {
            mLayerClient = layerClient;
        }

        public Builder setMessage(Message message) {
            mMessage = message;
            return this;
        }

        public Builder setContext(Context context) {
            mContext = context;
            return this;
        }

        public Builder setOneOnOne(boolean oneOnOne) {
            mIsOneOnOne = oneOnOne;
            return this;
        }

        public Builder setShouldShowAvatarInOneOnOneConversations(
                boolean shouldShowAvatarInOneOnOneConversations) {
            mShouldShowAvatarInOneOnOneConversations = shouldShowAvatarInOneOnOneConversations;
            return this;
        }

        public Builder setIsRecipientStatusPosition(Integer isRecipientStatusPosition) {
            mIsRecipientStatusPosition = isRecipientStatusPosition;
            return this;
        }

        public Builder setReadReceiptsEnabled(boolean readReceiptsEnabled) {
            mIsReadReceiptsEnabled = readReceiptsEnabled;
            return this;
        }

        public Builder setPosition(int position) {
            mPosition = position;
            return this;
        }

        public Builder setCellTypeMe(boolean cellTypeMe) {
            mIsCellTypeMe = cellTypeMe;
            return this;
        }

        public Builder setClusterSpaceVisible(boolean isClusterSpaceVisible) {
            mIsClusterSpaceVisible = isClusterSpaceVisible;
            return this;
        }

        public Builder setIsTimeGroupVisible(boolean isTimeGroupVisible) {
            mIsTimeGroupVisible = isTimeGroupVisible;
            return this;
        }

        public Builder setIsDisplayName(boolean isDisplayName) {
            mIsDisplayName = isDisplayName;
            return this;
        }

        public Builder setIsBindDateTimeForMessage(boolean isBindDateTimeForMessage) {
            mIsBindDateTimeForMessage = isBindDateTimeForMessage;
            return this;
        }

        public Builder setShouldClusterBeVisible(boolean shouldClusterBeVisible) {
            mShouldClusterBeVisible = shouldClusterBeVisible;
            return this;
        }

        public MessageItemViewModel build() {
            return new MessageItemViewModel(this);
        }
    }

    public static class AvatarViewDisplayWrapper{
        private boolean mOneOnOne;
        private boolean mShouldShowAvatarInOneOnOneConversations;
        private boolean mShouldMClusterBeDisplayed;

        public AvatarViewDisplayWrapper(boolean oneOnOne,
                boolean shouldShowAvatarInOneOnOneConversations, boolean shouldMClusterBeDisplayed) {
            mOneOnOne = oneOnOne;
            mShouldMClusterBeDisplayed = shouldMClusterBeDisplayed;
            mShouldShowAvatarInOneOnOneConversations = shouldShowAvatarInOneOnOneConversations;
        }
    }
}
