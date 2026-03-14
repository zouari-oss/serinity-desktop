package com.serinity.exercicecontrol.service;

import com.serinity.exercicecontrol.dao.ExerciseSessionDao;
import com.serinity.exercicecontrol.model.ExerciseSession;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ExerciseSessionService {

    private final ExerciseSessionDao sessionDao;

    public ExerciseSessionService() {
        this.sessionDao = new ExerciseSessionDao();
    }

    public int startSession(int userId, int exerciseId) throws SQLException {
        if (userId <= 0) throw new IllegalArgumentException("Invalid userId.");
        if (exerciseId <= 0) throw new IllegalArgumentException("Invalid exerciseId.");

        ExerciseSession s = new ExerciseSession();
        s.setUserId(userId);
        s.setExerciseId(exerciseId);
        s.setStatus("STARTED");
        s.setStartedAt(LocalDateTime.now());
        s.setCompletedAt(null);
        s.setFeedback(null);

        return sessionDao.insert(s);
    }

    public void completeSession(int sessionId, String feedback) throws SQLException {
        if (sessionId <= 0) throw new IllegalArgumentException("Invalid sessionId.");

        ExerciseSession s = sessionDao.findById(sessionId);
        if (s == null) throw new IllegalArgumentException("Session not found.");

        s.setStatus("COMPLETED");
        s.setCompletedAt(LocalDateTime.now());
        s.setFeedback(feedback);

        sessionDao.update(s);
    }

    public void abandonSession(int sessionId, String feedback) throws SQLException {
        if (sessionId <= 0) throw new IllegalArgumentException("Invalid sessionId.");

        ExerciseSession s = sessionDao.findById(sessionId);
        if (s == null) throw new IllegalArgumentException("Session not found.");

        s.setStatus("ABANDONED");
        s.setCompletedAt(LocalDateTime.now());
        s.setFeedback(feedback);

        sessionDao.update(s);
    }

    public ExerciseSession getSessionById(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Invalid session id.");
        return sessionDao.findById(id);
    }

    public List<ExerciseSession> getUserHistory(int userId) throws SQLException {
        if (userId <= 0) throw new IllegalArgumentException("Invalid userId.");
        return sessionDao.findByUserId(userId);
    }

    public List<ExerciseSession> getUserExerciseHistory(int userId, int exerciseId) throws SQLException {
        if (userId <= 0) throw new IllegalArgumentException("Invalid userId.");
        if (exerciseId <= 0) throw new IllegalArgumentException("Invalid exerciseId.");
        return sessionDao.findByUserAndExercise(userId, exerciseId);
    }
}
