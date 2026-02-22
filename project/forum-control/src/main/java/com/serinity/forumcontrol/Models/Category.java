package com.serinity.forumcontrol.Models;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private long id;
    private String name;
    private String slug;
    private String description;
    private Long parentId;

    // Association: One category can have many threads
    private List<Thread> threads;

    // Association: Category can have parent and children
    private Category parent;
    private List<Category> children;

    // Constructors
    public Category() {
        this.threads = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public Category(String name, String slug, String description, Long parentId) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.parentId = parentId;
        this.threads = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<Thread> getThreads() {
        return threads;
    }

    public void setThreads(List<Thread> threads) {
        this.threads = threads;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    // Association helper methods
    public void addThread(Thread thread) {
        if (!this.threads.contains(thread)) {
            this.threads.add(thread);
            thread.setCategory(this);
        }
    }

    public void removeThread(Thread thread) {
        if (this.threads.contains(thread)) {
            this.threads.remove(thread);
            thread.setCategory(null);
        }
    }

    public void addChild(Category child) {
        if (!this.children.contains(child)) {
            this.children.add(child);
            child.setParent(this);
        }
    }

    public void removeChild(Category child) {
        if (this.children.contains(child)) {
            this.children.remove(child);
            child.setParent(null);
        }
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", threadsCount=" + threads.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return slug != null && slug.equals(category.slug);
    }

    @Override
    public int hashCode() {
        return slug != null ? slug.hashCode() : 0;
    }
}