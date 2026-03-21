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

    private static final List<DefaultHabitDefinition> DEFAULT_HABITS = List.of(
            new DefaultHabitDefinition("Exercise", HabitType.CHECKBOX),
            new DefaultHabitDefinition("Meditation", HabitType.CHECKBOX),
            new DefaultHabitDefinition("Reading", HabitType.CHECKBOX),
            new DefaultHabitDefinition("Mood", HabitType.RATING),
            new DefaultHabitDefinition("Sleep", HabitType.RATING),
            new DefaultHabitDefinition("Stress", HabitType.RATING)
    );

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
        for (DefaultHabitDefinition defaultHabit : DEFAULT_HABITS) {
            habitDefinitionRepository.findByName(defaultHabit.name())
                    .orElseGet(() -> habitDefinitionRepository.save(
                            new HabitDefinition(defaultHabit.name(), defaultHabit.type())
                    ));
        }

        getOrCreateDayEntry(LocalDate.now());
    }

    public TrackerOverview getTrackerOverview() {
        DayEntry todayEntry = getOrCreateDayEntry(LocalDate.now());
        List<HabitDefinition> definitions = getHabitDefinitions();
        List<HabitRecord> todayRecords = getOrCreateHabitRecords(todayEntry, definitions);
        List<DayEntry> recentEntries = new ArrayList<>(dayEntryRepository.findTop31ByOrderByDateDesc());
        recentEntries.sort(Comparator.comparing(DayEntry::getDate));

        return new TrackerOverview(todayEntry, definitions, todayRecords, recentEntries);
    }

    @Transactional(readOnly = true)
    public List<MonthDayView> buildCurrentMonthView() {
        LocalDate currentDate = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(currentDate);
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

    public DayEntry updateDailyHighlight(LocalDate date, String dailyHighlight) {
        DayEntry dayEntry = getOrCreateDayEntry(date);
        dayEntry.setDailyHighlight(dailyHighlight == null || dailyHighlight.isBlank() ? null : dailyHighlight.trim());
        return dayEntryRepository.save(dayEntry);
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

    public List<HabitRecord> getOrCreateHabitRecords(DayEntry dayEntry, List<HabitDefinition> definitions) {
        List<HabitRecord> existingRecords = habitRecordRepository.findByDayEntryOrderByHabitDefinitionName(dayEntry);
        if (existingRecords.size() == definitions.size()) {
            return existingRecords;
        }

        for (HabitDefinition definition : definitions) {
            boolean recordExists = existingRecords.stream()
                    .anyMatch(record -> record.getHabitDefinition().getId().equals(definition.getId()));

            if (!recordExists) {
                HabitRecord habitRecord = new HabitRecord(dayEntry, definition);
                habitRecordRepository.save(habitRecord);
                existingRecords.add(habitRecord);
            }
        }

        existingRecords.sort(Comparator.comparing(record -> record.getHabitDefinition().getName()));
        return existingRecords;
    }

    public record TrackerOverview(
            DayEntry todayEntry,
            List<HabitDefinition> habitDefinitions,
            List<HabitRecord> todayRecords,
            List<DayEntry> recentEntries
    ) {
    }

    private record DefaultHabitDefinition(String name, HabitType type) {
    }
}
