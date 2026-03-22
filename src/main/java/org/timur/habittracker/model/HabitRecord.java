package org.timur.habittracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"day_entry_id", "habit_definition_id"})
)
public class HabitRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "day_entry_id", nullable = false)
    private DayEntry dayEntry;

    @ManyToOne(optional = false)
    @JoinColumn(name = "habit_definition_id", nullable = false)
    private HabitDefinition habitDefinition;

    @Column
    private Boolean checkedValue;

    @Column
    private Integer numericValue;

    public HabitRecord() {
    }

    public HabitRecord(DayEntry dayEntry, HabitDefinition habitDefinition) {
        this.dayEntry = dayEntry;
        this.habitDefinition = habitDefinition;
    }

    public Long getId() {
        return id;
    }

    public DayEntry getDayEntry() {
        return dayEntry;
    }

    public HabitDefinition getHabitDefinition() {
        return habitDefinition;
    }

    public void setHabitDefinition(HabitDefinition habitDefinition) {
        this.habitDefinition = habitDefinition;
    }

    public Boolean getCheckedValue() {
        return checkedValue;
    }

    public void setCheckedValue(Boolean checkedValue) {
        this.checkedValue = checkedValue;
    }

    public Integer getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(Integer numericValue) {
        this.numericValue = numericValue;
    }
}
