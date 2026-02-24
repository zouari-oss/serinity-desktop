package com.serinity.forumcontrol.CurrentUser;

public final class CurrentUser {

    private static final String CURRENT_USER_ID = "5";

    private CurrentUser() {}

    public static String getCurrentUserId() {
        return CURRENT_USER_ID;
    }
}
