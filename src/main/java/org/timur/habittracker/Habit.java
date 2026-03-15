package org.timur.habittracker;

public class Habit {
    private final long id;
    private String name;
    private String description;
    private boolean completedToday;

    public Habit(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.completedToday = false;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompletedToday() {
        return completedToday;
    }

    public void markCompleted() {
        this.completedToday = true;
    }

    public void resetForNewDay() {
        this.completedToday = false;
    }
}