package org.timur.habittracker;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private boolean completedToday;

    public Habit() {
    }

    public Habit(String name, String description) {
        this.name = name;
        this.description = description;
        this.completedToday = false;
    }

    public Long getId() {
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