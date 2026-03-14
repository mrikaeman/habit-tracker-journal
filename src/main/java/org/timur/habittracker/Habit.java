package org.timur.habittracker;

public class Habit {
    private String name;
    private String description;
    private int time; // how long per day
    private int frequency; // how often per week
    private boolean completedToday;


    public Habit(String name, String description, int time, int frequency) {
        this.name = name;
        this.description = description;
        this.time = time;
        this.frequency = frequency;
        this.completedToday = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getTime() {
        return time;
    }

    public int getFrequency() {
        return frequency;
    }

    public boolean isCompletedToday() {
        return completedToday;
    }

    public void completeToday() {
        completedToday = true;
    }
}
