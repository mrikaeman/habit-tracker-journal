package org.timur.habittracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.timur.habittracker.service.HabitTrackerService;

import java.time.LocalDate;
import java.time.YearMonth;

@Controller
public class HabitTrackerController {

    private final HabitTrackerService habitTrackerService;

    public HabitTrackerController(HabitTrackerService habitTrackerService) {
        this.habitTrackerService = habitTrackerService;
    }

    @GetMapping("/")
    public String showTracker(Model model) {
        model.addAttribute("habitDefinitions", habitTrackerService.getHabitDefinitions());
        model.addAttribute("monthRows", habitTrackerService.buildCurrentMonthView());
        model.addAttribute("currentMonth", YearMonth.now());
        return "tracker";
    }

    @PostMapping("/highlight")
    public String updateHighlight(@RequestParam LocalDate date,
                                  @RequestParam String dailyHighlight) {
        habitTrackerService.updateDailyHighlight(date, dailyHighlight);
        return "redirect:/";
    }
}
