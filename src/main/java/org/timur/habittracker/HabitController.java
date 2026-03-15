package org.timur.habittracker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HabitController {

    private final HabitTracker habitTracker;

    public HabitController(HabitTracker habitTracker) {
        this.habitTracker = habitTracker;
    }

    @GetMapping("/")
    public String showHabits(Model model) {
        model.addAttribute("habits", habitTracker.getHabits());
        return "habits";
    }

    @PostMapping("/add")
    public String addHabit(@RequestParam String name,
                           @RequestParam String description) {
        habitTracker.createHabit(name, description);
        return "redirect:/";
    }

    @PostMapping("/complete")
    public String completeHabit(@RequestParam Long id) {
        habitTracker.markHabitCompleted(id);
        return "redirect:/";
    }
}