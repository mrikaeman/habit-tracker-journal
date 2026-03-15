package org.timur.habittracker;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HabitTracker {

    private final HabitRepository habitRepository;

    public HabitTracker(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public Habit createHabit(String name, String description) {
        Habit habit = new Habit(name, description);
        return habitRepository.save(habit);
    }

    public List<Habit> getHabits() {
        return habitRepository.findAll();
    }

    public void markHabitCompleted(Long id) {
        Habit habit = habitRepository.findById(id).orElse(null);
        if (habit != null) {
            habit.markCompleted();
            habitRepository.save(habit);
        }
    }

    public void deleteHabit(Long id) {
        habitRepository.deleteById(id);
    }
}