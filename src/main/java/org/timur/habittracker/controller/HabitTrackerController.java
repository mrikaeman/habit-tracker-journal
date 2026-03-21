package org.timur.habittracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.timur.habittracker.model.HabitType;
import org.timur.habittracker.service.HabitTrackerService;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;

@Controller
public class HabitTrackerController {

    private final HabitTrackerService habitTrackerService;

    public HabitTrackerController(HabitTrackerService habitTrackerService) {
        this.habitTrackerService = habitTrackerService;
    }

    @GetMapping("/")
    public String showTracker(@RequestParam(required = false) YearMonth month, Model model) {
        YearMonth selectedMonth = month == null ? YearMonth.now() : month;

        model.addAttribute("monthRows", habitTrackerService.buildMonthView(selectedMonth));
        model.addAttribute("checkboxHabitDefinitions", habitTrackerService.getCheckboxHabitDefinitions());
        model.addAttribute("ratingHabitGraphs", habitTrackerService.buildMonthRatingGraphs(selectedMonth));
        model.addAttribute("monthOptions", habitTrackerService.buildMonthOptions(selectedMonth));
        model.addAttribute("monthPhotos", habitTrackerService.getMonthPhotos(selectedMonth));
        model.addAttribute("currentMonth", selectedMonth);
        model.addAttribute("currentMonthValue", selectedMonth.toString());
        return "tracker";
    }

    @GetMapping("/photos/view")
    public ResponseEntity<Resource> viewMonthPhoto(@RequestParam YearMonth month,
                                                   @RequestParam String file) {
        Path photoPath = habitTrackerService.getMonthPhotoPath(month, file);

        try {
            Resource resource = new UrlResource(photoPath.toUri());
            MediaType mediaType = MediaType.parseMediaType(habitTrackerService.resolvePhotoContentType(file));

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                    .body(resource);
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("Could not load saved photo.", exception);
        }
    }

    @PostMapping("/highlight")
    public String updateHighlight(@RequestParam LocalDate date,
                                  @RequestParam String dailyHighlight,
                                  @RequestParam(required = false) String month) {
        habitTrackerService.updateDailyHighlight(date, dailyHighlight);
        return redirectToMonth(month);
    }

    @PostMapping("/columns")
    public String createColumn(@RequestParam String name,
                               @RequestParam HabitType type,
                               @RequestParam(required = false) Integer ratingScaleMax,
                               @RequestParam(required = false) String month) {
        habitTrackerService.createHabitDefinition(name, type, ratingScaleMax);
        return redirectToMonth(month);
    }

    @PostMapping("/columns/rename")
    public String renameColumn(@RequestParam Long habitDefinitionId,
                               @RequestParam String name,
                               @RequestParam(required = false) String month) {
        habitTrackerService.renameHabitDefinition(habitDefinitionId, name);
        return redirectToMonth(month);
    }

    @PostMapping("/columns/delete")
    public String deleteColumn(@RequestParam Long habitDefinitionId,
                               @RequestParam(required = false) String month) {
        habitTrackerService.deleteHabitDefinition(habitDefinitionId);
        return redirectToMonth(month);
    }

    @PostMapping("/records/checkbox")
    public String updateCheckboxRecord(@RequestParam LocalDate date,
                                       @RequestParam Long habitDefinitionId,
                                       @RequestParam boolean checked,
                                       @RequestParam(required = false) Integer numericValue,
                                       @RequestParam(required = false) String month) {
        habitTrackerService.updateCheckboxRecord(date, habitDefinitionId, checked, numericValue);
        return redirectToMonth(month);
    }

    @PostMapping("/records/rating")
    public String updateRatingRecord(@RequestParam LocalDate date,
                                     @RequestParam Long habitDefinitionId,
                                     @RequestParam Integer numericValue,
                                     @RequestParam(required = false) String month) {
        habitTrackerService.updateRatingRecord(date, habitDefinitionId, numericValue);
        return redirectToMonth(month);
    }

    @PostMapping("/photos")
    public String addMonthPhotos(@RequestParam YearMonth month,
                                 @RequestParam("photos") MultipartFile[] photos) {
        habitTrackerService.addMonthPhotos(month, photos);
        return redirectToMonth(month.toString());
    }

    @PostMapping("/photos/delete")
    public String deleteMonthPhoto(@RequestParam YearMonth month,
                                   @RequestParam String file) {
        habitTrackerService.deleteMonthPhoto(month, file);
        return redirectToMonth(month.toString());
    }

    private String redirectToMonth(String month) {
        return month == null || month.isBlank() ? "redirect:/" : "redirect:/?month=" + month;
    }
}
