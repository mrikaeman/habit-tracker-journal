package org.timur.habittracker;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Habit workout = new Habit("Workout", "50 pushups, 30 pullups, stretching", 30, 5, false);
        Habit singing = new Habit("Singing", "singing exercizes", 30, 5, false);
        Habit meditation = new Habit("Meditation", "meditation exercizes (kundalini)", 45, 5, false);

        workout.completeToday();
        singing.completeToday();

        System.out.println("Habit: " + workout.getName());
        System.out.println("Description: " + workout.getDescription());
        System.out.println("Completed today: " + workout.isCompletedToday());

        System.out.println();

        System.out.println("Habit: " + singing.getName());
        System.out.println("Description: " + singing.getDescription());
        System.out.println("Completed today: " + singing.isCompletedToday());

        System.out.println();

        System.out.println("Habit: " + meditation.getName());
        System.out.println("Description: " + meditation.getDescription());
        System.out.println("Completed today: " + meditation.isCompletedToday());
    }
}