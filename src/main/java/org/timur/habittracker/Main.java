package org.timur.habittracker;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Habit workout = new Habit("Workout", "50 pushups, 30 pullups, stretching", 30, 5, false);
        Habit singing = new Habit("Singing", "singing exercizes", 30, 5, false);
        Habit meditation = new Habit("Meditation", "meditation exercizes (kundalini)", 45, 5, false);

        HabitTracker habitTracker = new HabitTracker();

        habitTracker.addHabit(workout);
        habitTracker.addHabit(singing);
        habitTracker.addHabit(meditation);

        habitTracker.markHabitCompleted("Workout");

        habitTracker.listHabits();
    }
}