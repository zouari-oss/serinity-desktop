package com.serinity.exercicecontrol.security;

public final class AuthContext {
    private static int userId = -1;
    private static String role = "USER"; // "ADMIN" ou "USER"

    private AuthContext() {}

    public static void login(int id, String r) {
        userId = id;
        role = (r == null) ? "USER" : r;
    }

    public static int userId() { return userId; }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public static String role() { return role; }
}