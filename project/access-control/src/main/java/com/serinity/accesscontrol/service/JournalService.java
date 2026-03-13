package com.serinity.accesscontrol.service;

import com.serinity.accesscontrol.ai.JournalAiTagger;
import com.serinity.accesscontrol.dao.JournalEntryDao;
import com.serinity.accesscontrol.model.JournalEntry;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class JournalService {

  public interface GuidedSerializer {
    String serialize(String a1, String a2, String a3);
  }

  private final JournalEntryDao dao;
  private final GuidedSerializer serializer;
  private final JournalAiTagger aiTagger;

  public JournalService(JournalEntryDao dao, GuidedSerializer serializer) {
    this(dao, serializer, new JournalAiTagger());
  }

  public JournalService(JournalEntryDao dao, GuidedSerializer serializer, JournalAiTagger aiTagger) {
    this.dao = Objects.requireNonNull(dao);
    this.serializer = Objects.requireNonNull(serializer);
    this.aiTagger = Objects.requireNonNull(aiTagger);
  }

  /** @return null if OK, otherwise an error message */
  public String create(String userId, String title, String a1, String a2, String a3) {
    if (userId == null || userId.isBlank())
      return "Invalid user.";

    String err = JournalValidation.validateNewOrEdit(title, a1, a2, a3);
    if (err != null)
      return err;

    JournalEntry e = new JournalEntry();
    e.setUserId(userId);
    e.setTitle(title.trim());
    e.setContent(serializer.serialize(a1.trim(), a2.trim(), a3.trim()));

    try {
      long id = dao.insert(e);
      if (id <= 0)
        return "Database error while saving.";

      // Auto-generate AI tags
      String aiJson = aiTagger.suggestTagsJson(e.getContent());
      LocalDateTime genAt = aiTagger.nowGeneratedAt();

      dao.updateAiFields(id, userId, aiJson, JournalAiTagger.MODEL_VERSION, genAt);

      return null;
    } catch (SQLException ex) {
      ex.printStackTrace();
      return "Database error while saving.";
    }
  }

  // dirty check
  public String update(String userId, JournalEntry editing, String title, String a1, String a2, String a3) {
    if (userId == null || userId.isBlank())
      return "Invalid user.";
    if (editing == null)
      return "Entry not found.";
    if (!Objects.equals(editing.getUserId(), userId))
      return "Forbidden.";

    String newTitle = title == null ? null : title.trim();
    String newContent = serializer.serialize(
        a1 == null ? null : a1.trim(),
        a2 == null ? null : a2.trim(),
        a3 == null ? null : a3.trim());

    boolean dirty = !Objects.equals(editing.getTitle(), newTitle) ||
        !Objects.equals(editing.getContent(), newContent);

    if (!dirty)
      return "No changes to save.";

    String err = JournalValidation.validateNewOrEdit(title, a1, a2, a3);
    if (err != null)
      return err;

    JournalEntry e = new JournalEntry();
    e.setId(editing.getId());
    e.setUserId(userId);
    e.setTitle(newTitle);
    e.setContent(newContent);

    try {
      boolean ok = dao.update(e);
      if (!ok)
        return "Entry not found.";

      // AI tags
      String aiJson = aiTagger.suggestTagsJson(e.getContent());
      LocalDateTime genAt = aiTagger.nowGeneratedAt();

      dao.updateAiFields(e.getId(), userId, aiJson, JournalAiTagger.MODEL_VERSION, genAt);

      return null;
    } catch (SQLException ex) {
      ex.printStackTrace();
      return "Database error while saving.";
    }
  }
}
