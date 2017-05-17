package com.layer.ui.avatar;

import com.layer.sdk.messaging.Identity;

public interface IdentityNameFormatter {
    /**
     * Implement to provide custom Identity Initials when there is no Image Url in the
     * Identity passed into the AvatarView
     * @see IdentityNameFormatterImpl for a sample implementation
     * @param identity
     * @return
     */
    String getInitials(Identity identity);
}
