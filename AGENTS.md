# Project: Habit Tracker Journal

## Tech stack
- Java 21
- Spring Boot
- Thymeleaf
- Spring Data JPA
- H2 database (local)

## Architecture
Packages are organized as:
- controller → web layer
- service → business logic
- repository → database access
- model → JPA entities
- view → UI helper classes (NOT entities)

Follow this structure strictly.

## Current domain model
- DayEntry → represents one calendar day
- HabitDefinition → defines what habits exist
- HabitRecord → stores values for a habit on a day
- HabitType → CHECKBOX or RATING

## Current goal
Build a monthly journal-style habit tracker:
- one row per day
- daily highlight
- checkbox habits
- rating habits

## Important constraints
- keep code simple and readable
- do not overengineer
- no authentication
- no cloud storage
- no AI summaries yet
- no advanced chart libraries yet

## UI direction
- Thymeleaf templates
- simple table-based monthly layout
- functionality first, styling later

## Coding style
- prefer explicit code over clever abstractions
- small methods
- clear naming
- keep controllers thin
- put logic in service layer

## Before making big changes
- explain plan first
- list files to change
- then implement