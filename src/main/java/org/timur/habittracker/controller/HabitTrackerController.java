package org.timur.habittracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.timur.habittracker.model.HabitType;
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
        model.addAttribute("monthRows", habitTrackerService.buildCurrentMonthView());
        model.addAttribute("checkboxHabitDefinitions", habitTrackerService.getCheckboxHabitDefinitions());
        model.addAttribute("ratingHabitGraphs", habitTrackerService.buildCurrentMonthRatingGraphs());
        model.addAttribute("currentMonth", YearMonth.now());
        return "tracker";
    }

    @PostMapping("/highlight")
    public String updateHighlight(@RequestParam LocalDate date,
                                  @RequestParam String dailyHighlight) {
        habitTrackerService.updateDailyHighlight(date, dailyHighlight);
        return "redirect:/";
    }

    @PostMapping("/columns")
    public String createColumn(@RequestParam String name,
                               @RequestParam HabitType type,
                               @RequestParam(required = false) Integer ratingScaleMax) {
        habitTrackerService.createHabitDefinition(name, type, ratingScaleMax);
        return "redirect:/";
    }

    @PostMapping("/columns/rename")
    public String renameColumn(@RequestParam Long habitDefinitionId,
                               @RequestParam String name) {
        habitTrackerService.renameHabitDefinition(habitDefinitionId, name);
        return "redirect:/";
    }

    @PostMapping("/columns/delete")
    public String deleteColumn(@RequestParam Long habitDefinitionId) {
        habitTrackerService.deleteHabitDefinition(habitDefinitionId);
        return "redirect:/";
    }

    @PostMapping("/records/checkbox")
    public String updateCheckboxRecord(@RequestParam LocalDate date,
                                       @RequestParam Long habitDefinitionId,
                                       @RequestParam boolean checked,
                                       @RequestParam(required = false) Integer numericValue) {
        habitTrackerService.updateCheckboxRecord(date, habitDefinitionId, checked, numericValue);
        return "redirect:/";
    }

    @PostMapping("/records/rating")
    public String updateRatingRecord(@RequestParam LocalDate date,
                                     @RequestParam Long habitDefinitionId,
                                     @RequestParam Integer numericValue) {
        habitTrackerService.updateRatingRecord(date, habitDefinitionId, numericValue);
        return "redirect:/";
    }
}
