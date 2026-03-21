package org.timur.habittracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.timur.habittracker.model.DayEntry;
import org.timur.habittracker.model.HabitDefinition;
import org.timur.habittracker.model.HabitRecord;
import org.timur.habittracker.model.HabitType;
import org.timur.habittracker.repository.DayEntryRepository;
import org.timur.habittracker.repository.HabitDefinitionRepository;
import org.timur.habittracker.repository.HabitRecordRepository;
import org.timur.habittracker.view.MonthDayView;
import org.timur.habittracker.view.MonthRatingGraphView;
import org.timur.habittracker.view.MonthRatingPointView;
import org.timur.habittracker.view.MonthRatingSegmentView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class HabitTrackerService {

    private final HabitDefinitionRepository habitDefinitionRepository;
    private final DayEntryRepository dayEntryRepository;
    private final HabitRecordRepository habitRecordRepository;

    public HabitTrackerService(HabitDefinitionRepository habitDefinitionRepository,
                               DayEntryRepository dayEntryRepository,
                               HabitRecordRepository habitRecordRepository) {
        this.habitDefinitionRepository = habitDefinitionRepository;
        this.dayEntryRepository = dayEntryRepository;
        this.habitRecordRepository = habitRecordRepository;
    }

    public void initializeDefaults() {
        getOrCreateDayEntry(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<MonthDayView> buildCurrentMonthView() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        LocalDate lastDayOfMonth = currentMonth.atEndOfMonth();

        List<MonthDayView> monthRows = new ArrayList<>();
        for (LocalDate date = firstDayOfMonth; !date.isAfter(lastDayOfMonth); date = date.plusDays(1)) {
            DayEntry dayEntry = dayEntryRepository.findByDate(date).orElse(null);
            String dailyHighlight = "";
            Map<Long, HabitRecord> recordsByHabitId = new HashMap<>();

            if (dayEntry != null) {
                dailyHighlight = dayEntry.getDailyHighlight() == null ? "" : dayEntry.getDailyHighlight();

                List<HabitRecord> records = habitRecordRepository.findByDayEntry(dayEntry);
                for (HabitRecord record : records) {
                    recordsByHabitId.put(record.getHabitDefinition().getId(), record);
                }
            }

            monthRows.add(new MonthDayView(date, dailyHighlight, recordsByHabitId));
        }

        return monthRows;
    }

    @Transactional(readOnly = true)
    public List<HabitDefinition> getCheckboxHabitDefinitions() {
        return getHabitDefinitions().stream()
                .filter(definition -> definition.getType() == HabitType.CHECKBOX)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MonthRatingGraphView> buildCurrentMonthRatingGraphs() {
        List<MonthDayView> monthRows = buildCurrentMonthView();
        List<HabitDefinition> ratingDefinitions = getHabitDefinitions().stream()
                .filter(definition -> definition.getType() == HabitType.RATING)
                .toList();

        List<MonthRatingGraphView> ratingGraphs = new ArrayList<>();
        for (HabitDefinition definition : ratingDefinitions) {
            ratingGraphs.add(buildRatingGraph(definition, monthRows));
        }

        return ratingGraphs;
    }

    public DayEntry updateDailyHighlight(LocalDate date, String dailyHighlight) {
        DayEntry dayEntry = getOrCreateDayEntry(date);
        dayEntry.setDailyHighlight(dailyHighlight == null || dailyHighlight.isBlank() ? null : dailyHighlight.trim());
        return dayEntryRepository.save(dayEntry);
    }

    public HabitDefinition createHabitDefinition(String name, HabitType type, Integer ratingScaleMax) {
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Column name is required.");
        }

        if (habitDefinitionRepository.findByName(normalizedName).isPresent()) {
            throw new IllegalArgumentException("A column with that name already exists.");
        }

        Integer normalizedScale = null;
        if (type == HabitType.RATING) {
            if (ratingScaleMax == null || ratingScaleMax < 1) {
                throw new IllegalArgumentException("Rating scale must be at least 1.");
            }
            normalizedScale = ratingScaleMax;
        }

        return habitDefinitionRepository.save(new HabitDefinition(normalizedName, type, normalizedScale));
    }

    public HabitDefinition renameHabitDefinition(Long habitDefinitionId, String name) {
        HabitDefinition habitDefinition = getHabitDefinition(habitDefinitionId);
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Column name is required.");
        }

        HabitDefinition existingDefinition = habitDefinitionRepository.findByName(normalizedName).orElse(null);
        if (existingDefinition != null && !existingDefinition.getId().equals(habitDefinitionId)) {
            throw new IllegalArgumentException("A column with that name already exists.");
        }

        habitDefinition.setName(normalizedName);
        return habitDefinitionRepository.save(habitDefinition);
    }

    public void deleteHabitDefinition(Long habitDefinitionId) {
        HabitDefinition habitDefinition = getHabitDefinition(habitDefinitionId);
        habitRecordRepository.deleteByHabitDefinition(habitDefinition);
        habitDefinitionRepository.delete(habitDefinition);
    }

    public HabitRecord updateCheckboxRecord(LocalDate date, Long habitDefinitionId, boolean checked, Integer numericValue) {
        HabitDefinition habitDefinition = getHabitDefinition(habitDefinitionId);
        DayEntry dayEntry = getOrCreateDayEntry(date);
        HabitRecord habitRecord = getOrCreateHabitRecord(dayEntry, habitDefinition);

        if (habitDefinition.getType() != HabitType.CHECKBOX) {
            throw new IllegalArgumentException("The selected column is not a checkbox column.");
        }

        if (!checked) {
            habitRecord.setCheckedValue(false);
            habitRecord.setNumericValue(null);
            return habitRecordRepository.save(habitRecord);
        }

        if (numericValue == null || numericValue < 1 || numericValue > 10) {
            throw new IllegalArgumentException("Checkbox ratings must be between 1 and 10.");
        }

        habitRecord.setCheckedValue(true);
        habitRecord.setNumericValue(numericValue);
        return habitRecordRepository.save(habitRecord);
    }

    public HabitRecord updateRatingRecord(LocalDate date, Long habitDefinitionId, Integer numericValue) {
        HabitDefinition habitDefinition = getHabitDefinition(habitDefinitionId);
        DayEntry dayEntry = getOrCreateDayEntry(date);
        HabitRecord habitRecord = getOrCreateHabitRecord(dayEntry, habitDefinition);

        if (habitDefinition.getType() != HabitType.RATING) {
            throw new IllegalArgumentException("The selected column is not a rating column.");
        }

        Integer scaleMax = habitDefinition.getRatingScaleMax();
        if (numericValue == null || numericValue < 1 || scaleMax == null || numericValue > scaleMax) {
            throw new IllegalArgumentException("Rating must be within the configured scale.");
        }

        habitRecord.setCheckedValue(null);
        habitRecord.setNumericValue(numericValue);
        return habitRecordRepository.save(habitRecord);
    }

    @Transactional(readOnly = true)
    public List<HabitDefinition> getHabitDefinitions() {
        return habitDefinitionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(HabitDefinition::getName))
                .toList();
    }

    public DayEntry getOrCreateDayEntry(LocalDate date) {
        return dayEntryRepository.findByDate(date)
                .orElseGet(() -> dayEntryRepository.save(new DayEntry(date)));
    }

    public HabitDefinition getHabitDefinition(Long habitDefinitionId) {
        return habitDefinitionRepository.findById(habitDefinitionId)
                .orElseThrow(() -> new IllegalArgumentException("Column not found."));
    }

    public HabitRecord getOrCreateHabitRecord(DayEntry dayEntry, HabitDefinition habitDefinition) {
        return habitRecordRepository.findByDayEntryAndHabitDefinition(dayEntry, habitDefinition)
                .orElseGet(() -> habitRecordRepository.save(new HabitRecord(dayEntry, habitDefinition)));
    }

    private MonthRatingGraphView buildRatingGraph(HabitDefinition definition, List<MonthDayView> monthRows) {
        int scaleMin = 1;
        int scaleMax = definition.getRatingScaleMax() == null || definition.getRatingScaleMax() < scaleMin
                ? 10
                : definition.getRatingScaleMax();

        List<MonthRatingPointView> points = new ArrayList<>();
        List<MonthRatingSegmentView> segments = new ArrayList<>();
        MonthRatingPointView previousPoint = null;
        int previousDayIndex = -1;

        for (int dayIndex = 0; dayIndex < monthRows.size(); dayIndex++) {
            MonthDayView row = monthRows.get(dayIndex);
            HabitRecord record = row.getRecordsByHabitId().get(definition.getId());
            if (record == null || record.getNumericValue() == null) {
                previousPoint = null;
                previousDayIndex = -1;
                continue;
            }

            int value = clamp(record.getNumericValue(), scaleMin, scaleMax);
            double x = calculateX(value, scaleMin, scaleMax);
            double y = ((dayIndex + 0.5) / monthRows.size()) * 100.0;

            MonthRatingPointView point = new MonthRatingPointView(row.getDate(), value, x, y);
            points.add(point);

            if (previousPoint != null && previousDayIndex + 1 == dayIndex) {
                segments.add(new MonthRatingSegmentView(
                        previousPoint.getX(),
                        previousPoint.getY(),
                        point.getX(),
                        point.getY()
                ));
            }

            previousPoint = point;
            previousDayIndex = dayIndex;
        }

        return new MonthRatingGraphView(definition, scaleMin, scaleMax, points, segments);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double calculateX(int value, int scaleMin, int scaleMax) {
        if (scaleMax <= scaleMin) {
            return 50.0;
        }

        return ((double) (value - scaleMin) / (scaleMax - scaleMin)) * 100.0;
    }
}
