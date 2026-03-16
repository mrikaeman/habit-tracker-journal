package org.timur.habittracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.timur.habittracker.service.HabitTrackerService;

import java.time.LocalDate;

@Controller
public class HabitTrackerController {

    private final HabitTrackerService habitTrackerService;

    public HabitTrackerController(HabitTrackerService habitTrackerService) {
        this.habitTrackerService = habitTrackerService;
    }

    @GetMapping("/")
    public String showTracker(Model model) {
        HabitTrackerService.TrackerOverview overview = habitTrackerService.getTrackerOverview();
        model.addAttribute("todayEntry", overview.todayEntry());
        model.addAttribute("habitDefinitions", overview.habitDefinitions());
        model.addAttribute("todayRecords", overview.todayRecords());
        model.addAttribute("recentEntries", overview.recentEntries());
        return "tracker";
    }

    @PostMapping("/highlight")
    public String updateHighlight(@RequestParam String dailyHighlight) {
        habitTrackerService.updateDailyHighlight(LocalDate.now(), dailyHighlight);
        return "redirect:/";
    }
}
