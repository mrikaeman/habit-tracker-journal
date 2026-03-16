package org.timur.habittracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.timur.habittracker.model.DayEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DayEntryRepository extends JpaRepository<DayEntry, Long> {
    Optional<DayEntry> findByDate(LocalDate date);

    List<DayEntry> findTop31ByOrderByDateDesc();
}
