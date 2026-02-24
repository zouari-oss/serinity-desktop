package com.serinity.exercicecontrol.service;

import com.serinity.exercicecontrol.dao.ResourceDao;
import com.serinity.exercicecontrol.model.Resource;

import java.sql.SQLException;
import java.util.List;

public class ResourceService {

    private final ResourceDao resourceDao;

    public ResourceService() {
        this.resourceDao = new ResourceDao();
    }

    public int addResource(Resource r) throws SQLException {
        validateResource(r);
        return resourceDao.insert(r);
    }

    public void updateResource(Resource r) throws SQLException {
        if (r.getId() <= 0) throw new IllegalArgumentException("Resource id is required for update.");
        validateResource(r);
        resourceDao.update(r);
    }

    public void deleteResource(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Invalid resource id.");
        resourceDao.delete(id);
    }

    public Resource getResourceById(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Invalid resource id.");
        return resourceDao.findById(id);
    }

    public List<Resource> getAllResources() throws SQLException {
        return resourceDao.findAll();
    }

    public List<Resource> getResourcesByExerciseId(int exerciseId) throws SQLException {
        if (exerciseId <= 0) throw new IllegalArgumentException("Invalid exercise id.");
        return resourceDao.findByExerciseId(exerciseId);
    }

    private void validateResource(Resource r) {
        if (r == null) throw new IllegalArgumentException("Resource cannot be null.");

        if (r.getTitle() == null || r.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Resource title is required.");

        if (r.getMediaType() == null || r.getMediaType().trim().isEmpty())
            throw new IllegalArgumentException("Media type is required.");

        // Must have at least url OR content (depends on mediaType)
        boolean hasUrl = r.getUrl() != null && !r.getUrl().trim().isEmpty();
        boolean hasContent = r.getContent() != null && !r.getContent().trim().isEmpty();

        if (!hasUrl && !hasContent)
            throw new IllegalArgumentException("Resource must have either a URL or content.");

        if (r.getDurationSeconds() < 0)
            throw new IllegalArgumentException("Duration seconds cannot be negative.");

        if (r.getExerciseId() <= 0)
            throw new IllegalArgumentException("exerciseId is required.");
    }
}
