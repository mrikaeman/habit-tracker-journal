package org.timur.habittracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//import java.util.Scanner;
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        /*
        HabitTracker tracker = new HabitTracker();
        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        while (running) {
            System.out.println("\nCommands: add, list, complete, exit");
            System.out.print("> ");
            String command = scanner.nextLine();

            switch (command.toLowerCase()) {
                case "add":
                    System.out.print("Name: ");
                    String name = scanner.nextLine();

                    System.out.print("Description: ");
                    String description = scanner.nextLine();

                    System.out.println("How long per day (minutes)? ");
                    int time = Integer.parseInt(scanner.nextLine());

                    System.out.println("How often per week? ");
                    int frequency = Integer.parseInt(scanner.nextLine());

                    Habit habit = new Habit(name, description, time, frequency);
                    tracker.addHabit(habit);
                    System.out.println("Habit added.");
                    break;

                case "list":
                    tracker.listHabits();
                    break;

                case "complete":
                    System.out.print("Enter habit name: ");
                    String habitName = scanner.nextLine();
                    tracker.markHabitCompleted(habitName);
                    break;

                case "exit":
                    running = false;
                    System.out.println("Goodbye.");
                    break;

                default:
                    System.out.println("Unknown command.");
            }
        }

        scanner.close();
         */ //old application
        SpringApplication.run(Main.class, args);
    }
}
