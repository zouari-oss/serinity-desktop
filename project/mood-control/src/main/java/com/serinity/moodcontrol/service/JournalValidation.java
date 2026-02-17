package com.serinity.moodcontrol.service;

import java.util.Set;

public final class JournalValidation {

    private static final Set<Character> FORBIDDEN = Set.of('*', '{', '}', '|', '[', ']', '#');

    private JournalValidation() {}

    public static String validateNewOrEdit(String title, String a1, String a2, String a3) {
        String err;

        err = validateField(title, "journal.field.title", 80);
        if (err != null) return err;

        err = validateField(a1, "journal.field.a1", 500);
        if (err != null) return err;

        err = validateField(a2, "journal.field.a2", 500);
        if (err != null) return err;

        err = validateField(a3, "journal.field.a3", 500);
        if (err != null) return err;

        return null;
    }


    private static String validateField(String value, String fieldKey, int maxLen) {
        if (value == null) return fieldKey + "|journal.validation.required";

        String v = value.trim();
        if (v.isEmpty()) return fieldKey + "|journal.validation.empty";

        v = v.replace("\r\n", "\n");
        if (v.length() > maxLen) return fieldKey + "|journal.validation.max_len";

        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (FORBIDDEN.contains(c)) return fieldKey + "|journal.validation.forbidden";
        }
        return null;
    }

}
