package com.serinity.exercicecontrol.service;

import com.serinity.exercicecontrol.dao.SessionDAO;
import com.serinity.exercicecontrol.dao.SessionDAO.SessionEntity;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;

public class SessionService {

    private final SessionDAO dao;

    public SessionService(SessionDAO dao) {
        this.dao = dao;
    }

    // CREATED -> IN_PROGRESS
    public void start(int sessionId) {
        try {
            dao.withTransaction(() -> {
                SessionEntity s = mustLoad(sessionId);

                if (s.status() != SessionStatus.CREATED) {
                    throw new SessionStateException("Start interdit: status=" + s.status());
                }

                LocalDateTime now = LocalDateTime.now();

                SessionEntity updated = new SessionEntity(
                        s.id(),
                        SessionStatus.IN_PROGRESS,
                        now,
                        null,
                        s.feedback(),
                        0,
                        now
                );

                validate(updated);
                dao.update(updated);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // IN_PROGRESS -> PAUSED
    public void pause(int sessionId) {
        try {
            dao.withTransaction(() -> {
                SessionEntity s = mustLoad(sessionId);

                if (s.status() != SessionStatus.IN_PROGRESS) {
                    throw new SessionStateException("Pause interdite: status=" + s.status());
                }

                LocalDateTime now = LocalDateTime.now();
                int added = secondsBetween(s.lastResumedAt(), now);
                int newActive = s.activeSeconds() + Math.max(0, added);

                SessionEntity updated = new SessionEntity(
                        s.id(),
                        SessionStatus.PAUSED,
                        s.startedAt(),
                        null,
                        s.feedback(),
                        newActive,
                        null
                );

                validate(updated);
                dao.update(updated);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // PAUSED -> IN_PROGRESS
    public void resume(int sessionId) {
        try {
            dao.withTransaction(() -> {
                SessionEntity s = mustLoad(sessionId);

                if (s.status() != SessionStatus.PAUSED) {
                    throw new SessionStateException("Resume interdit: status=" + s.status());
                }

                LocalDateTime now = LocalDateTime.now();

                SessionEntity updated = new SessionEntity(
                        s.id(),
                        SessionStatus.IN_PROGRESS,
                        s.startedAt(),
                        null,
                        s.feedback(),
                        s.activeSeconds(),
                        now
                );

                validate(updated);
                dao.update(updated);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // IN_PROGRESS -> COMPLETED
    public void complete(int sessionId, String feedback) {
        try {
            dao.withTransaction(() -> {
                SessionEntity s = mustLoad(sessionId);

                if (s.status() != SessionStatus.IN_PROGRESS) {
                    throw new SessionStateException("Complete interdit: status=" + s.status());
                }

                LocalDateTime now = LocalDateTime.now();
                int added = secondsBetween(s.lastResumedAt(), now);
                int newActive = s.activeSeconds() + Math.max(0, added);

                SessionEntity updated = new SessionEntity(
                        s.id(),
                        SessionStatus.COMPLETED,
                        s.startedAt(),
                        now,
                        feedback,
                        newActive,
                        null
                );

                validate(updated);
                dao.update(updated);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // IN_PROGRESS -> ABORTED
    public void abort(int sessionId) {
        try {
            dao.withTransaction(() -> {
                SessionEntity s = mustLoad(sessionId);

                if (s.status() != SessionStatus.IN_PROGRESS) {
                    throw new SessionStateException("Abort interdit: status=" + s.status());
                }

                LocalDateTime now = LocalDateTime.now();
                int added = secondsBetween(s.lastResumedAt(), now);
                int newActive = s.activeSeconds() + Math.max(0, added);

                SessionEntity updated = new SessionEntity(
                        s.id(),
                        SessionStatus.ABORTED,
                        s.startedAt(),
                        now,
                        s.feedback(),
                        newActive,
                        null
                );

                validate(updated);
                dao.update(updated);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- helpers ----------------

    private SessionEntity mustLoad(int sessionId) throws SQLException {
        SessionEntity s = dao.findByIdForUpdate(sessionId);
        if (s == null) throw new SessionStateException("Session introuvable id=" + sessionId);
        return s;
    }

    private void validate(SessionEntity s) {
        switch (s.status()) {
            case CREATED -> {
                if (s.startedAt() != null || s.completedAt() != null)
                    throw new SessionStateException("CREATED invalide: dates non null");
            }
            case IN_PROGRESS -> {
                if (s.startedAt() == null || s.completedAt() != null || s.lastResumedAt() == null)
                    throw new SessionStateException("IN_PROGRESS invalide");
            }
            case PAUSED -> {
                if (s.startedAt() == null || s.completedAt() != null || s.lastResumedAt() != null)
                    throw new SessionStateException("PAUSED invalide");
            }
            case COMPLETED -> {
                if (s.startedAt() == null || s.completedAt() == null || s.activeSeconds() <= 0)
                    throw new SessionStateException("COMPLETED invalide");
            }
            case ABORTED, CANCELLED -> {
                if (s.startedAt() == null)
                    throw new SessionStateException(s.status() + " invalide: started_at null");
            }
        }
        if (s.activeSeconds() < 0) throw new SessionStateException("active_seconds < 0");
    }

    private int secondsBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return 0;
        long sec = Duration.between(from, to).getSeconds();
        if (sec > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) sec;
    }
}