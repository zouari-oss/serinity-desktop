package com.serinity.forumcontrol.CurrentUser;

public final class CurrentUser {

    private static final String CURRENT_USER_ID = "6affa2df-dda9-442d-99ee-d2a3c1e78c64";

    private CurrentUser() {}

    public static String getCurrentUserId() {
        return CURRENT_USER_ID;
    }
}
