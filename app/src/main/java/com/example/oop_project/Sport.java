package com.example.oop_project;

public class Sport {
    private String name;
    private String description;
    private int maxParticipants;

    public Sport(String name, String description, int maxParticipants) {
        this.name = name;
        this.description = description;
        this.maxParticipants = maxParticipants;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
}
