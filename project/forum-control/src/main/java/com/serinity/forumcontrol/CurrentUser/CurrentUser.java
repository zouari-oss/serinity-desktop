package com.serinity.forumcontrol.HardcodedUser;

public final class FakeUser {

    private static final String CURRENT_USER_ID = "5";

    private FakeUser() {}

    public static String getCurrentUserId() {
        return CURRENT_USER_ID;
    }
}
