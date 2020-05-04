package com.example.oop_project;

public class Hall {
    private String id;
    private int maxSize;

    public Hall(String id, int maxSize) {
        this.id = id;
        this.maxSize = maxSize;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
