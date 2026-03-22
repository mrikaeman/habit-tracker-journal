package org.timur.habittracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class DayEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(length = 500)
    private String dailyHighlight;

    @Column(length = 4000)
    private String dailyThought;

    public DayEntry() {
    }

    public DayEntry(LocalDate date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDailyHighlight() {
        return dailyHighlight;
    }

    public void setDailyHighlight(String dailyHighlight) {
        this.dailyHighlight = dailyHighlight;
    }

    public String getDailyThought() {
        return dailyThought;
    }

    public void setDailyThought(String dailyThought) {
        this.dailyThought = dailyThought;
    }
}
