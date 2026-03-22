package org.timur.habittracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class HabitDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HabitType type;

    @Column
    private Integer ratingScaleMax;

    @Column
    private String monthKey;

    public HabitDefinition() {
    }

    public HabitDefinition(String name, HabitType type) {
        this.name = name;
        this.displayName = name;
        this.type = type;
    }

    public HabitDefinition(String name, HabitType type, Integer ratingScaleMax) {
        this.name = name;
        this.displayName = name;
        this.type = type;
        this.ratingScaleMax = ratingScaleMax;
    }

    public HabitDefinition(String name, String displayName, HabitType type, Integer ratingScaleMax, String monthKey) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.ratingScaleMax = ratingScaleMax;
        this.monthKey = monthKey;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName == null || displayName.isBlank() ? name : displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public HabitType getType() {
        return type;
    }

    public Integer getRatingScaleMax() {
        return ratingScaleMax;
    }

    public String getMonthKey() {
        return monthKey;
    }

    public void setMonthKey(String monthKey) {
        this.monthKey = monthKey;
    }
}
