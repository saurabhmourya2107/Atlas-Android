package com.layer.ui.avatar;

import com.layer.sdk.messaging.Identity;
import com.layer.ui.util.Util;

public class IdentityNameFormatterImpl implements IdentityNameFormatter {
    @Override
    public String getInitials(Identity identity) {
        return Util.getInitials(identity);
    }
}
