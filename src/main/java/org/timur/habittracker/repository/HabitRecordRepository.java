package org.timur.habittracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.timur.habittracker.model.DayEntry;
import org.timur.habittracker.model.HabitDefinition;
import org.timur.habittracker.model.HabitRecord;

import java.util.List;
import java.util.Optional;

public interface HabitRecordRepository extends JpaRepository<HabitRecord, Long> {
    List<HabitRecord> findByDayEntry(DayEntry dayEntry);

    List<HabitRecord> findByDayEntryOrderByHabitDefinitionName(DayEntry dayEntry);

    Optional<HabitRecord> findByDayEntryAndHabitDefinition(DayEntry dayEntry, HabitDefinition habitDefinition);

    List<HabitRecord> findByHabitDefinition(HabitDefinition habitDefinition);

    void deleteByHabitDefinition(HabitDefinition habitDefinition);
}
