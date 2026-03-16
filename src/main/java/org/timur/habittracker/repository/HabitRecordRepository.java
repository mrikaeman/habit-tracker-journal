package org.timur.habittracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.timur.habittracker.model.DayEntry;
import org.timur.habittracker.model.HabitRecord;

import java.util.List;

public interface HabitRecordRepository extends JpaRepository<HabitRecord, Long> {
    List<HabitRecord> findByDayEntryOrderByHabitDefinitionName(DayEntry dayEntry);
}
