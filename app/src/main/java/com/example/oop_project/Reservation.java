package com.example.oop_project;

public class Reservation {
    private String id;
    private String startTime;
    private String endTime;
    private String hall;
    private String owner;
    private String description;
    private String sport = null;
    private int maxParticipants;

    public Reservation(String id, String startTime, String endTime, String hall, String owner, String description, int maxParticipants) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hall = hall;
        this.owner = owner;
        this.description = description;
        this.maxParticipants = maxParticipants;
    }

    public Reservation(String id, String startTime, String endTime, String hall, String owner, String description, int maxParticipants, String sport) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hall = hall;
        this.owner = owner;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.sport = sport;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getHall() {
        return hall;
    }

    public void setHall(String hall) {
        this.hall = hall;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }
}
