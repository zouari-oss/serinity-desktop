package com.serinity.moodcontrol.service;

import com.serinity.moodcontrol.dao.JournalEntryDao;
import com.serinity.moodcontrol.model.JournalEntry;

import java.sql.SQLException;
import java.util.Objects;

public class JournalService {

    public interface GuidedSerializer {
        String serialize(String a1, String a2, String a3);
    }

    private final JournalEntryDao dao;
    private final GuidedSerializer serializer;

    public JournalService(JournalEntryDao dao, GuidedSerializer serializer) {
        this.dao = Objects.requireNonNull(dao);
        this.serializer = Objects.requireNonNull(serializer);
    }

    /** @return null if OK, otherwise an error message */
    public String create(long userId, String title, String a1, String a2, String a3) {
        if (userId <= 0) return "Invalid user.";

        String err = JournalValidation.validateNewOrEdit(title, a1, a2, a3);
        if (err != null) return err;

        JournalEntry e = new JournalEntry();
        e.setUserId(userId);
        e.setTitle(title.trim());
        e.setContent(serializer.serialize(a1.trim(), a2.trim(), a3.trim()));

        try {
            dao.insert(e);
            return null;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "Database error while saving.";
        }
    }

    /** Edit must be dirty AND pass same rules as entry. */
    public String update(long userId, JournalEntry editing, String title, String a1, String a2, String a3) {
        if (userId <= 0) return "Invalid user.";
        if (editing == null) return "Entry not found.";
        if (editing.getUserId() != userId) return "Forbidden.";

        String newTitle = title == null ? null : title.trim();
        String newContent = serializer.serialize(
                a1 == null ? null : a1.trim(),
                a2 == null ? null : a2.trim(),
                a3 == null ? null : a3.trim()
        );

        boolean dirty =
                !Objects.equals(editing.getTitle(), newTitle) ||
                        !Objects.equals(editing.getContent(), newContent);

        if (!dirty) return "No changes to save.";

        String err = JournalValidation.validateNewOrEdit(title, a1, a2, a3);
        if (err != null) return err;

        JournalEntry e = new JournalEntry();
        e.setId(editing.getId());
        e.setUserId(userId);
        e.setTitle(newTitle);
        e.setContent(newContent);

        try {
            dao.update(e);
            return null;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "Database error while saving.";
        }
    }
}
