package org.timur.habittracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.timur.habittracker.model.HabitDefinition;

import java.util.List;
import java.util.Optional;

public interface HabitDefinitionRepository extends JpaRepository<HabitDefinition, Long> {
    Optional<HabitDefinition> findByName(String name);

    List<HabitDefinition> findByMonthKey(String monthKey);

    Optional<HabitDefinition> findByMonthKeyAndDisplayNameIgnoreCase(String monthKey, String displayName);

    List<HabitDefinition> findByMonthKeyIsNull();
}
