package com.layer.ui.messageitem;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.FrameLayout;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.ui.R;
import com.layer.ui.adapters.MessagesAdapter;
import com.layer.ui.util.Util;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MessageItemViewModel {
    private final LayerClient mLayerClient;
    private MessagesAdapter.Cluster mCluster;
    private Message mMessage;
    private Context mContext;
    private boolean mOneOnOne;
    private boolean mShouldShowAvatarInOneOnOneConversations;
    private final DateFormat mTimeFormat;
    private Integer mRecipientStatusPosition;
    private boolean mReadReceiptsEnabled;
    private int mPosition;
    private boolean mIsCellTypeMe;
    private int mReadCount = 0;
    private Map<Identity, Message.RecipientStatus> mStatuses;
    private boolean delivered;

    public MessageItemViewModel(Builder builder) {
        mMessage = builder.mMessage;
        mCluster = builder.mCluster;
        mContext = builder.mContext;
        mOneOnOne = builder.mOneOnOne;
        mLayerClient = builder.mLayerClient;
        mPosition = builder.mPosition;
        mIsCellTypeMe = builder.mIsCellTypeMe;
        mRecipientStatusPosition = builder.mRecipientStatusPosition;
        mReadReceiptsEnabled = builder.mReadReceiptsEnabled;
        mStatuses = mMessage.getRecipientStatus();
        mShouldShowAvatarInOneOnOneConversations = builder.mShouldShowAvatarInOneOnOneConversations;
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(mContext);
        updateValuesForRecipient();
    }

    private void updateValuesForRecipient() {
        if (mIsCellTypeMe && (mReadReceiptsEnabled && mRecipientStatusPosition != null && mRecipientStatusPosition == mPosition)) {
            delivered = false;
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
                        delivered = true;
                        break;
                }
            }
        }
    }

    public boolean isClusterSpaceVisible() {
        if (mCluster.mClusterWithPrevious == null) {
            // No previous message, so no gap
            return false;
        } else if (mCluster.mDateBoundaryWithPrevious || mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.MORE_THAN_HOUR) {
            // Crossed into a new day, or > 1hr lull in conversation
            return false;
        } else if (mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.LESS_THAN_MINUTE) {
            // Same sender with < 1m gap
            return false;
        } else if (mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.NEW_SENDER || mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.LESS_THAN_HOUR) {
            // New sender or > 1m gap
            return true;
        }

        return false;
    }

    public boolean isTimeGroupVisible() {
        if (mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.LESS_THAN_MINUTE) {
            return false;
        } else if (mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.NEW_SENDER ||
                mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.LESS_THAN_HOUR) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isMessageSent() {
        return mMessage.isSent();
    }

    public String getSender() {
        Identity identity = mMessage.getSender();
        if (identity == null) {
            return  mContext.getString(R.string.layer_ui_message_item_unknown_user);
        }
        return Util.getDisplayName(mMessage.getSender());
    }

    public boolean isDisplayName() {
        return !mOneOnOne && (mCluster.mClusterWithPrevious == null
                || mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.NEW_SENDER);
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
        boolean shouldClusterBeVisible = mCluster.mClusterWithNext == null
                || mCluster.mClusterWithNext != MessagesAdapter.ClusterType.LESS_THAN_MINUTE;
        return new AvatarViewDisplayWrapper(mOneOnOne, mShouldShowAvatarInOneOnOneConversations,
                shouldClusterBeVisible);
    }

    public Set<Identity> getParticipants() {
        return new HashSet<>(Arrays.asList(mMessage.getSender()));
    }

    public String getTimeGroupDay() {
        Date receivedAt = mMessage.getReceivedAt();
        if (receivedAt == null) receivedAt = new Date();
        return Util.formatTimeDay(mContext, receivedAt);
    }

    public String getGroupTime() {
        Date receivedAt = mMessage.getReceivedAt();
        return mTimeFormat.format(receivedAt.getTime());
    }

    public boolean isBindDateTimeForMessage() {

        if (mCluster.mClusterWithPrevious == null) {
            // No previous message, so no gap
            return true;
        } else if (mCluster.mDateBoundaryWithPrevious || mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.MORE_THAN_HOUR) {
            // Crossed into a new day, or > 1hr lull in conversation
            return true;
        } else if (mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.LESS_THAN_MINUTE) {
            // Same sender with < 1m gap
            return false;
        } else if (mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.NEW_SENDER || mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.LESS_THAN_HOUR) {
            // New sender or > 1m gap
            return false;
        }
        return false;
    }

    public boolean isRecipientStatusVisible() {
        return mIsCellTypeMe && (mReadReceiptsEnabled && mRecipientStatusPosition != null && mRecipientStatusPosition == mPosition);
    }

    public String getRecipientStatus() {
        if (mIsCellTypeMe && (mReadReceiptsEnabled && mRecipientStatusPosition != null && mRecipientStatusPosition == mPosition)) {
            if (mReadCount > 0) {
                // Use 2 to include one other participant plus the current user
                if (mStatuses.size() > 2) {
                    return mContext.getResources()
                            .getQuantityString(R.plurals.layer_ui_message_item_read_muliple_participants,
                                    mReadCount, mReadCount);
                } else {
                    return mContext.getString(R.string.layer_ui_message_item_read);
                }
            } else if (delivered) {
                return mContext.getString(R.string.layer_ui_message_item_delivered);
            }
        }
        return "";
    }

    public static class Builder {
        private final LayerClient mLayerClient;
        private MessagesAdapter.Cluster mCluster;
        private Message mMessage;
        private Context mContext;
        private boolean mOneOnOne;
        private boolean mShouldShowAvatarInOneOnOneConversations;
        private Integer mRecipientStatusPosition;
        private boolean mReadReceiptsEnabled;
        private int mPosition;
        private boolean mIsCellTypeMe;

        public Builder(LayerClient layerClient) {
            mLayerClient = layerClient;
        }

        public Builder setCluster(MessagesAdapter.Cluster cluster) {
            mCluster = cluster;
            return this;
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
            mOneOnOne = oneOnOne;
            return this;
        }

        public Builder setShouldShowAvatarInOneOnOneConversations(
                boolean shouldShowAvatarInOneOnOneConversations) {
            mShouldShowAvatarInOneOnOneConversations = shouldShowAvatarInOneOnOneConversations;
            return this;
        }

        public Builder setRecipientStatusPosition(Integer recipientStatusPosition) {
            mRecipientStatusPosition = recipientStatusPosition;
            return this;
        }

        public Builder setReadReceiptsEnabled(boolean readReceiptsEnabled) {
            mReadReceiptsEnabled = readReceiptsEnabled;
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

        public MessageItemViewModel build() {
            return new MessageItemViewModel(this);
        }
    }

    public static class AvatarViewDisplayWrapper{
        private boolean mOneOnOne;
        private boolean mShouldShowAvatarInOneOnOneConversations;
        private boolean mShouldMClusterBeDisplayed;

        public AvatarViewDisplayWrapper(boolean oneOnOne,
                boolean shouldShowAvatarInOneOnOneConversations,
                boolean shouldMClusterBeDisplayed) {
            mOneOnOne = oneOnOne;
            mShouldShowAvatarInOneOnOneConversations = shouldShowAvatarInOneOnOneConversations;
            mShouldMClusterBeDisplayed = shouldMClusterBeDisplayed;
        }
    }
}
