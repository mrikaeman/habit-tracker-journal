package org.timur.habittracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.timur.habittracker.model.HabitDefinition;

import java.util.Optional;

public interface HabitDefinitionRepository extends JpaRepository<HabitDefinition, Long> {
    Optional<HabitDefinition> findByName(String name);
}
