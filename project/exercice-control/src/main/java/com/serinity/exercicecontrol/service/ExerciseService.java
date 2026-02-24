package com.serinity.exercicecontrol.service;

import com.serinity.exercicecontrol.dao.ExerciseDao;
import com.serinity.exercicecontrol.model.Exercise;

import java.sql.SQLException;
import java.util.List;

public class ExerciseService {

    private final ExerciseDao exerciseDao;

    public ExerciseService() {
        this.exerciseDao = new ExerciseDao();
    }

    public int addExercise(Exercise ex) throws SQLException {
        validateExercise(ex);
        return exerciseDao.insert(ex);
    }

    public void updateExercise(Exercise ex) throws SQLException {
        if (ex.getId() <= 0) throw new IllegalArgumentException("Exercise id is required for update.");
        validateExercise(ex);
        exerciseDao.update(ex);
    }

    public void deleteExercise(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Invalid exercise id.");
        exerciseDao.delete(id);
    }

    public Exercise getExerciseById(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Invalid exercise id.");
        return exerciseDao.findById(id);
    }

    public List<Exercise> getAllExercises() throws SQLException {
        return exerciseDao.findAll();
    }

    public List<Exercise> getExercisesByType(String type) throws SQLException {
        if (type == null || type.trim().isEmpty()) throw new IllegalArgumentException("Type is required.");
        return exerciseDao.findByType(type.trim());
    }

    public List<Exercise> getExercisesByLevel(int level) throws SQLException {
        if (level < 1 || level > 5) throw new IllegalArgumentException("Level must be between 1 and 5.");
        return exerciseDao.findByLevel(level);
    }

    private void validateExercise(Exercise ex) {
        if (ex == null) throw new IllegalArgumentException("Exercise cannot be null.");

        if (ex.getTitle() == null || ex.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Title is required.");

        if (ex.getType() == null || ex.getType().trim().isEmpty())
            throw new IllegalArgumentException("Type is required.");

        if (ex.getLevel() < 1 || ex.getLevel() > 5)
            throw new IllegalArgumentException("Level must be between 1 and 5.");

        if (ex.getDurationMinutes() <= 0)
            throw new IllegalArgumentException("Duration must be > 0 minutes.");
    }
}
