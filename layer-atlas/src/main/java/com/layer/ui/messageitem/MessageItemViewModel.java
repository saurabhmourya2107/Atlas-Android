package com.layer.ui.messageitem;

import android.content.Context;
import android.databinding.BaseObservable;
import android.view.View;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.ui.R;
import com.layer.ui.adapters.MessagesAdapter;
import com.layer.ui.util.Util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MessageItemViewModel extends BaseObservable {
    private MessagesAdapter.Cluster mCluster;
    private Message mMessage;
    private Context mContext;
    private boolean mOneOnOne;
    private boolean mShouldShowAvatarInOneOnOneConversations;



    public MessageItemViewModel(Context context, Message message, MessagesAdapter.Cluster cluster,
            boolean oneOnOne, boolean shouldShowAvatarInOneOnOneConversations) {
        mMessage = message;
        mCluster = cluster;
        mContext = context;
        mOneOnOne = oneOnOne;
        mShouldShowAvatarInOneOnOneConversations = shouldShowAvatarInOneOnOneConversations;
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

    public boolean shouldDisplayName() {
        return !mOneOnOne && (mCluster.mClusterWithPrevious == null
                || mCluster.mClusterWithPrevious == MessagesAdapter.ClusterType.NEW_SENDER);
    }

    //TODO : Rewrite method......
    public int shouldDisplayAvatar() {
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

   /* //==============================================================================================
    // Clustering
    //==============================================================================================
    // TODO: optimize by limiting search to positions in- and around- visible range
    private Cluster getClustering(Message message, int position) {
        Cluster result = mClusterCache.get(message.getId());
        if (result == null) {
            result = new Cluster();
            mClusterCache.put(message.getId(), result);
        }

        int previousPosition = position - 1;
        Message previousMessage = (previousPosition >= 0) ? getItem(previousPosition) : null;
        if (previousMessage != null) {
            result.mDateBoundaryWithPrevious = isDateBoundary(previousMessage.getReceivedAt(), message.getReceivedAt());
            result.mClusterWithPrevious = MessagesAdapter.ClusterType.fromMessages(previousMessage, message);

            Cluster previousCluster = mClusterCache.get(previousMessage.getId());
            if (previousCluster == null) {
                previousCluster = new MessagesAdapter.Cluster();
                mClusterCache.put(previousMessage.getId(), previousCluster);
            } else {
                // does the previous need to change its clustering?
                if ((previousCluster.mClusterWithNext != result.mClusterWithPrevious) ||
                        (previousCluster.mDateBoundaryWithNext != result.mDateBoundaryWithPrevious)) {
                    requestUpdate(previousMessage, previousPosition);
                }
            }
            previousCluster.mClusterWithNext = result.mClusterWithPrevious;
            previousCluster.mDateBoundaryWithNext = result.mDateBoundaryWithPrevious;
        }

        int nextPosition = position + 1;
        Message nextMessage = (nextPosition < getItemCount()) ? getItem(nextPosition) : null;
        if (nextMessage != null) {
            result.mDateBoundaryWithNext = isDateBoundary(message.getReceivedAt(), nextMessage.getReceivedAt());
            result.mClusterWithNext = MessagesAdapter.ClusterType.fromMessages(message, nextMessage);

            MessagesAdapter.Cluster nextCluster = mClusterCache.get(nextMessage.getId());
            if (nextCluster == null) {
                nextCluster = new MessagesAdapter.Cluster();
                mClusterCache.put(nextMessage.getId(), nextCluster);
            } else {
                // does the next need to change its clustering?
                if ((nextCluster.mClusterWithPrevious != result.mClusterWithNext) ||
                        (nextCluster.mDateBoundaryWithPrevious != result.mDateBoundaryWithNext)) {
                    requestUpdate(nextMessage, nextPosition);
                }
            }
            nextCluster.mClusterWithPrevious = result.mClusterWithNext;
            nextCluster.mDateBoundaryWithPrevious = result.mDateBoundaryWithNext;
        }

        return result;
    }

    private static class Cluster {
        public boolean mDateBoundaryWithPrevious;
        public ClusterType mClusterWithPrevious;

        public boolean mDateBoundaryWithNext;
        public ClusterType mClusterWithNext;
    }

    private enum ClusterType {
        NEW_SENDER,
        LESS_THAN_MINUTE,
        LESS_THAN_HOUR,
        MORE_THAN_HOUR;

        private static final long MILLIS_MINUTE = 60 * 1000;
        private static final long MILLIS_HOUR = 60 * MILLIS_MINUTE;

        public static ClusterType fromMessages(Message older, Message newer) {
            // Different users?
            if (!older.getSender().equals(newer.getSender())) return NEW_SENDER;

            // Time clustering for same user?
            Date oldReceivedAt = older.getReceivedAt();
            Date newReceivedAt = newer.getReceivedAt();
            if (oldReceivedAt == null || newReceivedAt == null) return LESS_THAN_MINUTE;
            long delta = Math.abs(newReceivedAt.getTime() - oldReceivedAt.getTime());
            if (delta <= MILLIS_MINUTE) return LESS_THAN_MINUTE;
            if (delta <= MILLIS_HOUR) return LESS_THAN_HOUR;
            return MORE_THAN_HOUR;
        }
    }*/

}
