package org.timur.habittracker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.timur.habittracker.service.HabitTrackerService;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner seedDefaults(HabitTrackerService habitTrackerService) {
        return args -> habitTrackerService.initializeDefaults();
    }
}
