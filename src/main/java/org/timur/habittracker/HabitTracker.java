package org.timur.habittracker;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HabitTracker {
    private final List<Habit> habits;
    private long nextId;

    public HabitTracker() {
        this.habits = new ArrayList<>();
        this.nextId = 1;
    }

    public Habit createHabit(String name, String description) {
        Habit habit = new Habit(nextId, name, description);
        habits.add(habit);
        nextId++;
        return habit;
    }

    public void addHabit(Habit habit) {
        habits.add(habit);
    }

    public void removeHabitById(long id) {
        habits.removeIf(habit -> habit.getId() == id);
    }

    public List<Habit> getHabits() {
        return new ArrayList<>(habits);
    }

    public void clearHabits() {
        habits.clear();
    }

    public void markHabitCompleted(long id) {
        for (Habit habit : habits) {
            if (habit.getId() == id) {
                habit.markCompleted();
                return;
            }
        }
    }
}