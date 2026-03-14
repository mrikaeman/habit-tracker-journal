package org.timur.habittracker;

import java.util.ArrayList;
import java.util.List;

public class HabitTracker {
    private final List<Habit> habits;

    public HabitTracker(List<Habit> habits) {
        this.habits = habits;
    }

    public HabitTracker() {
        this.habits = new ArrayList<>();
    }

    public void addHabit(Habit habit) {
        habits.add(habit);
    }

    public void removeHabit(Habit habit) {
        habits.remove(habit);
    }

    public List<Habit> getHabits() {
        return new ArrayList<>(habits);
    }

    public void clearHabits() {
        habits.clear();
    }

    public void listHabits() {
        for (Habit habit : habits) {
            System.out.println("Habit: " + habit.getName());
            System.out.println("Description: " + habit.getDescription());
            System.out.println("Completed today: " + habit.isCompletedToday());
            System.out.println();
        }
    }

    public void markHabitCompleted(String name)
    {
        for (Habit habit : habits) {
            if (habit.getName().equalsIgnoreCase(name)) {
                habit.completeToday();
                break;
            }
        }
    }
}
