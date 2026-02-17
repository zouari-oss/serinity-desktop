package com.serinity.moodcontrol.service;

import java.util.Set;

public final class JournalValidation {

    private static final Set<Character> FORBIDDEN = Set.of('*', '{', '}', '|', '[', ']', '#');

    private JournalValidation() {}

    public static String validateNewOrEdit(String title, String a1, String a2, String a3) {
        String err;
        err = validateField(title, "Title", 80);     if (err != null) return err;
        err = validateField(a1, "Answer 1", 500);   if (err != null) return err;
        err = validateField(a2, "Answer 2", 500);   if (err != null) return err;
        err = validateField(a3, "Answer 3", 500);   if (err != null) return err;
        return null;
    }

    private static String validateField(String value, String name, int maxLen) {
        if (value == null) return name + " is required.";

        String v = value.trim();
        if (v.isEmpty()) return name + " cannot be empty.";

        v = v.replace("\r\n", "\n");
        if (v.length() > maxLen) return name + " must be ≤ " + maxLen + " characters.";

        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (FORBIDDEN.contains(c)) return name + " contains forbidden character: '" + c + "'.";
        }
        return null;
    }
}
