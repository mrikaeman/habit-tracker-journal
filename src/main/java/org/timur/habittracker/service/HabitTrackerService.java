package org.timur.habittracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.timur.habittracker.model.DayEntry;
import org.timur.habittracker.model.HabitDefinition;
import org.timur.habittracker.model.HabitRecord;
import org.timur.habittracker.model.HabitType;
import org.timur.habittracker.repository.DayEntryRepository;
import org.timur.habittracker.repository.HabitDefinitionRepository;
import org.timur.habittracker.repository.HabitRecordRepository;
import org.timur.habittracker.view.MonthDayView;
import org.timur.habittracker.view.MonthOptionView;
import org.timur.habittracker.view.MonthPhotoView;
import org.timur.habittracker.view.MonthRatingGraphView;
import org.timur.habittracker.view.MonthRatingPointView;
import org.timur.habittracker.view.MonthRatingSegmentView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class HabitTrackerService {

    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final Path MONTH_PHOTO_ROOT = Paths.get("data", "month-photos");
    private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".heic", ".heif"
    );
    private static final Set<String> BROWSER_PREVIEW_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"
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
        getOrCreateDayEntry(LocalDate.now());
    }

    public List<MonthDayView> buildMonthView(YearMonth month) {
        ensureMonthScopedHabitDefinitions();
        LocalDate firstDayOfMonth = month.atDay(1);
        LocalDate lastDayOfMonth = month.atEndOfMonth();

        List<MonthDayView> monthRows = new ArrayList<>();
        for (LocalDate date = firstDayOfMonth; !date.isAfter(lastDayOfMonth); date = date.plusDays(1)) {
            DayEntry dayEntry = dayEntryRepository.findByDate(date).orElse(null);
            String dailyHighlight = "";
            String dailyThought = "";
            Map<Long, HabitRecord> recordsByHabitId = new HashMap<>();

            if (dayEntry != null) {
                dailyHighlight = dayEntry.getDailyHighlight() == null ? "" : dayEntry.getDailyHighlight();
                dailyThought = dayEntry.getDailyThought() == null ? "" : dayEntry.getDailyThought();

                List<HabitRecord> records = habitRecordRepository.findByDayEntry(dayEntry);
                for (HabitRecord record : records) {
                    recordsByHabitId.put(record.getHabitDefinition().getId(), record);
                }
            }

            monthRows.add(new MonthDayView(date, dailyHighlight, dailyThought, recordsByHabitId));
        }

        return monthRows;
    }

    public List<HabitDefinition> getCheckboxHabitDefinitions(YearMonth month) {
        return getHabitDefinitions(month).stream()
                .filter(definition -> definition.getType() == HabitType.CHECKBOX)
                .toList();
    }

    public List<MonthRatingGraphView> buildMonthRatingGraphs(YearMonth month) {
        List<MonthDayView> monthRows = buildMonthView(month);
        List<HabitDefinition> ratingDefinitions = getHabitDefinitions(month).stream()
                .filter(definition -> definition.getType() == HabitType.RATING)
                .toList();

        List<MonthRatingGraphView> ratingGraphs = new ArrayList<>();
        for (HabitDefinition definition : ratingDefinitions) {
            ratingGraphs.add(buildRatingGraph(definition, monthRows));
        }

        return ratingGraphs;
    }

    public List<MonthOptionView> buildMonthOptions(YearMonth selectedMonth) {
        ensureMonthScopedHabitDefinitions();
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstEntryMonth = dayEntryRepository.findFirstByOrderByDateAsc()
                .map(dayEntry -> YearMonth.from(dayEntry.getDate()))
                .orElse(currentMonth);
        YearMonth firstPhotoMonth = findFirstPhotoMonth().orElse(firstEntryMonth);
        YearMonth firstAvailableMonth = firstPhotoMonth.isBefore(firstEntryMonth) ? firstPhotoMonth : firstEntryMonth;
        YearMonth lastAvailableMonth = currentMonth.plusMonths(2);

        if (selectedMonth.isBefore(firstAvailableMonth)) {
            selectedMonth = firstAvailableMonth;
        }
        if (selectedMonth.isAfter(lastAvailableMonth)) {
            selectedMonth = lastAvailableMonth;
        }

        List<MonthOptionView> monthOptions = new ArrayList<>();
        for (YearMonth month = firstAvailableMonth; !month.isAfter(lastAvailableMonth); month = month.plusMonths(1)) {
            monthOptions.add(new MonthOptionView(
                    month.toString(),
                    MONTH_LABEL_FORMATTER.format(month.atDay(1)),
                    month.equals(selectedMonth)
            ));
        }

        return monthOptions;
    }

    @Transactional(readOnly = true)
    public List<MonthPhotoView> getMonthPhotos(YearMonth month) {
        List<MonthPhotoView> monthPhotos = new ArrayList<>();
        Path monthDirectory = getMonthPhotoDirectory(month);
        if (!Files.isDirectory(monthDirectory)) {
            return monthPhotos;
        }

        try (var paths = Files.list(monthDirectory).sorted()) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        MonthPhotoView monthPhoto = buildMonthPhotoView(path);
                        if (monthPhoto != null) {
                            monthPhotos.add(monthPhoto);
                        }
                    });
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load month photos.", exception);
        }

        return monthPhotos;
    }

    public void addMonthPhotos(YearMonth month, MultipartFile[] photos) {
        if (photos == null || photos.length == 0) {
            return;
        }

        Path monthDirectory = getMonthPhotoDirectory(month);

        try {
            Files.createDirectories(monthDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not prepare month photo storage.", exception);
        }

        for (MultipartFile photo : photos) {
            if (photo == null || photo.isEmpty()) {
                continue;
            }

            String contentType = photo.getContentType();
            String extension = resolveFileExtension(photo.getOriginalFilename(), contentType);
            if (!isSupportedImageFile(contentType, extension)) {
                throw new IllegalArgumentException("Only image uploads are supported.");
            }

            String fileName = UUID.randomUUID() + extension;
            Path targetPath = monthDirectory.resolve(fileName);

            try {
                Files.copy(photo.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new IllegalStateException("Could not save uploaded photo.", exception);
            }
        }
    }

    @Transactional(readOnly = true)
    public Path getMonthPhotoPath(YearMonth month, String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Photo file name is required.");
        }

        Path monthDirectory = getMonthPhotoDirectory(month).normalize().toAbsolutePath();
        Path photoPath = monthDirectory.resolve(fileName).normalize().toAbsolutePath();

        if (!photoPath.startsWith(monthDirectory) || !Files.isRegularFile(photoPath)) {
            throw new IllegalArgumentException("Photo not found.");
        }

        return photoPath;
    }

    public void deleteMonthPhoto(YearMonth month, String fileName) {
        Path photoPath = getMonthPhotoPath(month, fileName);

        try {
            Files.deleteIfExists(photoPath);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not delete saved photo.", exception);
        }
    }

    public DayEntry updateDailyHighlight(LocalDate date, String dailyHighlight) {
        DayEntry dayEntry = getOrCreateDayEntry(date);
        dayEntry.setDailyHighlight(dailyHighlight == null || dailyHighlight.isBlank() ? null : dailyHighlight.trim());
        return dayEntryRepository.save(dayEntry);
    }

    public DayEntry updateDailyThought(LocalDate date, String dailyThought) {
        DayEntry dayEntry = getOrCreateDayEntry(date);
        dayEntry.setDailyThought(dailyThought == null || dailyThought.isBlank() ? null : dailyThought.trim());
        return dayEntryRepository.save(dayEntry);
    }

    public HabitDefinition createHabitDefinition(String name,
                                                 HabitType type,
                                                 Integer ratingScaleMax,
                                                 YearMonth month,
                                                 boolean allVisibleMonths) {
        ensureMonthScopedHabitDefinitions();
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Column name is required.");
        }

        Integer normalizedScale = null;
        if (type == HabitType.RATING) {
            if (ratingScaleMax == null || ratingScaleMax < 1) {
                throw new IllegalArgumentException("Rating scale must be at least 1.");
            }
            normalizedScale = ratingScaleMax;
        }

        List<YearMonth> targetMonths = allVisibleMonths
                ? getAvailableMonths()
                : List.of(month);

        HabitDefinition createdDefinition = null;
        for (YearMonth targetMonth : targetMonths) {
            validateDisplayNameAvailable(targetMonth, normalizedName, null);
            HabitDefinition habitDefinition = createMonthHabitDefinition(targetMonth, normalizedName, type, normalizedScale);
            if (targetMonth.equals(month)) {
                createdDefinition = habitDefinition;
            }
        }

        return createdDefinition;
    }

    public HabitDefinition renameHabitDefinition(Long habitDefinitionId, String name, YearMonth month) {
        ensureMonthScopedHabitDefinitions();
        HabitDefinition habitDefinition = getHabitDefinition(habitDefinitionId, month);
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Column name is required.");
        }

        validateDisplayNameAvailable(month, normalizedName, habitDefinitionId);
        habitDefinition.setDisplayName(normalizedName);
        return habitDefinitionRepository.save(habitDefinition);
    }

    public void deleteHabitDefinition(Long habitDefinitionId, YearMonth month) {
        ensureMonthScopedHabitDefinitions();
        HabitDefinition habitDefinition = getHabitDefinition(habitDefinitionId, month);
        habitRecordRepository.deleteByHabitDefinition(habitDefinition);
        habitDefinitionRepository.delete(habitDefinition);
    }

    public HabitRecord updateCheckboxRecord(LocalDate date, Long habitDefinitionId, boolean checked, Integer numericValue) {
        ensureMonthScopedHabitDefinitions();
        HabitDefinition habitDefinition = getHabitDefinition(habitDefinitionId, YearMonth.from(date));
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
        ensureMonthScopedHabitDefinitions();
        HabitDefinition habitDefinition = getHabitDefinition(habitDefinitionId, YearMonth.from(date));
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

    public List<HabitDefinition> getHabitDefinitions(YearMonth month) {
        ensureMonthScopedHabitDefinitions();
        return habitDefinitionRepository.findByMonthKey(month.toString())
                .stream()
                .sorted(Comparator.comparing(HabitDefinition::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public DayEntry getOrCreateDayEntry(LocalDate date) {
        return dayEntryRepository.findByDate(date)
                .orElseGet(() -> dayEntryRepository.save(new DayEntry(date)));
    }

    public HabitDefinition getHabitDefinition(Long habitDefinitionId, YearMonth month) {
        HabitDefinition habitDefinition = habitDefinitionRepository.findById(habitDefinitionId)
                .orElseThrow(() -> new IllegalArgumentException("Column not found."));

        if (!month.toString().equals(habitDefinition.getMonthKey())) {
            throw new IllegalArgumentException("Column not found in this month.");
        }

        return habitDefinition;
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

    private Optional<YearMonth> findFirstPhotoMonth() {
        if (!Files.isDirectory(MONTH_PHOTO_ROOT)) {
            return Optional.empty();
        }

        try (var paths = Files.list(MONTH_PHOTO_ROOT).sorted()) {
            return paths.filter(Files::isDirectory)
                    .map(this::parseYearMonthDirectory)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not inspect saved month photos.", exception);
        }
    }

    private Optional<YearMonth> parseYearMonthDirectory(Path path) {
        try {
            return Optional.of(YearMonth.parse(path.getFileName().toString()));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private Path getMonthPhotoDirectory(YearMonth month) {
        return MONTH_PHOTO_ROOT.resolve(month.toString());
    }

    private MonthPhotoView buildMonthPhotoView(Path path) {
        String fileName = path.getFileName().toString();
        if (!isSupportedImageFile(null, getFileExtension(fileName))) {
            return null;
        }

        return new MonthPhotoView(fileName, isBrowserPreviewable(fileName));
    }

    private String resolveFileExtension(String originalFilename, String contentType) {
        String originalExtension = getFileExtension(originalFilename);
        if (SUPPORTED_IMAGE_EXTENSIONS.contains(originalExtension)) {
            return originalExtension;
        }

        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            case "image/heic" -> ".heic";
            case "image/heif" -> ".heif";
            default -> ".jpg";
        };
    }

    public String resolvePhotoContentType(String fileName) {
        return switch (getFileExtension(fileName)) {
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            case ".bmp" -> "image/bmp";
            case ".heic" -> "image/heic";
            case ".heif" -> "image/heif";
            default -> "image/jpeg";
        };
    }

    private boolean isBrowserPreviewable(String fileName) {
        return BROWSER_PREVIEW_EXTENSIONS.contains(getFileExtension(fileName));
    }

    private boolean isSupportedImageFile(String contentType, String extension) {
        if (contentType != null && contentType.startsWith("image/")) {
            return true;
        }

        return SUPPORTED_IMAGE_EXTENSIONS.contains(extension);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }

        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex < 0) {
            return "";
        }

        return fileName.substring(extensionIndex).toLowerCase(Locale.ENGLISH);
    }

    private void ensureMonthScopedHabitDefinitions() {
        List<HabitDefinition> legacyDefinitions = habitDefinitionRepository.findByMonthKeyIsNull();
        if (legacyDefinitions.isEmpty()) {
            return;
        }

        List<YearMonth> availableMonths = getAvailableMonths();
        for (HabitDefinition legacyDefinition : legacyDefinitions) {
            Map<YearMonth, HabitDefinition> definitionsByMonth = new HashMap<>();
            for (YearMonth month : availableMonths) {
                HabitDefinition monthDefinition = createMonthHabitDefinition(
                        month,
                        legacyDefinition.getDisplayName(),
                        legacyDefinition.getType(),
                        legacyDefinition.getRatingScaleMax()
                );
                definitionsByMonth.put(month, monthDefinition);
            }

            List<HabitRecord> legacyRecords = habitRecordRepository.findByHabitDefinition(legacyDefinition);
            for (HabitRecord legacyRecord : legacyRecords) {
                YearMonth recordMonth = YearMonth.from(legacyRecord.getDayEntry().getDate());
                HabitDefinition targetDefinition = definitionsByMonth.get(recordMonth);
                if (targetDefinition == null) {
                    targetDefinition = createMonthHabitDefinition(
                            recordMonth,
                            legacyDefinition.getDisplayName(),
                            legacyDefinition.getType(),
                            legacyDefinition.getRatingScaleMax()
                    );
                    definitionsByMonth.put(recordMonth, targetDefinition);
                }

                legacyRecord.setHabitDefinition(targetDefinition);
                habitRecordRepository.save(legacyRecord);
            }

            habitDefinitionRepository.delete(legacyDefinition);
        }
    }

    private HabitDefinition createMonthHabitDefinition(YearMonth month,
                                                       String displayName,
                                                       HabitType type,
                                                       Integer ratingScaleMax) {
        HabitDefinition existingDefinition = habitDefinitionRepository
                .findByMonthKeyAndDisplayNameIgnoreCase(month.toString(), displayName)
                .orElse(null);
        if (existingDefinition != null) {
            return existingDefinition;
        }

        HabitDefinition habitDefinition = new HabitDefinition(
                buildStoredHabitName(month, displayName),
                displayName,
                type,
                ratingScaleMax,
                month.toString()
        );
        return habitDefinitionRepository.save(habitDefinition);
    }

    private void validateDisplayNameAvailable(YearMonth month, String displayName, Long currentHabitDefinitionId) {
        HabitDefinition existingDefinition = habitDefinitionRepository
                .findByMonthKeyAndDisplayNameIgnoreCase(month.toString(), displayName)
                .orElse(null);
        if (existingDefinition != null && !existingDefinition.getId().equals(currentHabitDefinitionId)) {
            throw new IllegalArgumentException("A column with that name already exists in this month.");
        }
    }

    private String buildStoredHabitName(YearMonth month, String displayName) {
        return month + "::" + displayName + "::" + UUID.randomUUID();
    }

    private List<YearMonth> getAvailableMonths() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstEntryMonth = dayEntryRepository.findFirstByOrderByDateAsc()
                .map(dayEntry -> YearMonth.from(dayEntry.getDate()))
                .orElse(currentMonth);
        YearMonth firstPhotoMonth = findFirstPhotoMonth().orElse(firstEntryMonth);
        YearMonth firstAvailableMonth = firstPhotoMonth.isBefore(firstEntryMonth) ? firstPhotoMonth : firstEntryMonth;
        YearMonth lastAvailableMonth = currentMonth.plusMonths(2);

        List<YearMonth> months = new ArrayList<>();
        for (YearMonth month = firstAvailableMonth; !month.isAfter(lastAvailableMonth); month = month.plusMonths(1)) {
            months.add(month);
        }

        return months;
    }
}
