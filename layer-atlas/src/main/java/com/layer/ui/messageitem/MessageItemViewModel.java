package com.layer.ui.messageitem;

import android.content.Context;
import android.view.View;

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



    public MessageItemViewModel(Context context, LayerClient layerClient, Message message,
            MessagesAdapter.Cluster cluster, boolean oneOnOne, int position,
            boolean shouldShowAvatarInOneOnOneConversations, int recipientStatusPosition, boolean readReceiptsEnabled, boolean isCellTypeMe) {
        mMessage = message;
        mCluster = cluster;
        mContext = context;
        mOneOnOne = oneOnOne;
        mLayerClient = layerClient;
        mPosition = position;
        mIsCellTypeMe = isCellTypeMe;
        mRecipientStatusPosition = recipientStatusPosition;
        mReadReceiptsEnabled = readReceiptsEnabled;
        mStatuses = mMessage.getRecipientStatus();
        mShouldShowAvatarInOneOnOneConversations = shouldShowAvatarInOneOnOneConversations;
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
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
        return mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.NEW_SENDER ||
                mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.LESS_THAN_HOUR;
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

    //TODO : Rewrite method......
    public int isDisplayAvatar() {
        if (mOneOnOne) {
            if (mShouldShowAvatarInOneOnOneConversations) {
                return View.VISIBLE;
            } else {
                return View.GONE;
            }
        } else if (mCluster.mClusterWithNext == null
                || mCluster.mClusterWithNext != MessagesAdapter.ClusterType.LESS_THAN_MINUTE) {
            return View.VISIBLE;
        } else {
            return View.VISIBLE;
        }
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
        return (mCluster.mClusterWithPrevious == null)
                || mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.MORE_THAN_HOUR;
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
}
