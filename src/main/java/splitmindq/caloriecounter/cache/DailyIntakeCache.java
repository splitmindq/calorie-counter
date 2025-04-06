package splitmindq.caloriecounter.cache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import splitmindq.caloriecounter.listener.DailyIntakeDeletedEvent;
import splitmindq.caloriecounter.model.DailyIntake;

@Component
public class DailyIntakeCache {
    private static final long INTAKES_WITH_DATE_TTL = 10;
    private static final long INTAKES_WITHOUT_DATE_TTL = 1;
    private static final long NUTRITION_DATA_TTL = 30;

    private static final int INTAKES_WITH_DATE_LIMIT = 100;
    private static final int INTAKES_WITHOUT_DATE_LIMIT = 100;

    private final Map<String, Map<LocalDate, CacheEntry<List<DailyIntake>>>> intakesWithDateCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<List<DailyIntake>>> intakesWithoutDateCache = new ConcurrentHashMap<>();
    private final Map<String, Map<LocalDate, CacheEntry<Map<String, Double>>>> nutritionCache = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    private static final Logger logger = LoggerFactory.getLogger(DailyIntakeCache.class);

    public DailyIntakeCache() {
        this.cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.MINUTES);
    }

    private static class CacheEntry<V> {
        @Getter
        private final V value;
        @Getter
        private final LocalDateTime createdAt;
        private final long ttlMinutes;

        CacheEntry(V value, long ttlMinutes) {
            this.value = value;
            this.createdAt = LocalDateTime.now();
            this.ttlMinutes = ttlMinutes;
        }

        boolean isExpired() {
            return createdAt.plusMinutes(ttlMinutes).isBefore(LocalDateTime.now());
        }
    }

    private void cleanupExpiredEntries() {
        cleanCacheWithDate(intakesWithDateCache);
        cleanSimpleCache(intakesWithoutDateCache);
        cleanCacheWithDate(nutritionCache);
    }

    private <K1, K2, V> void cleanCacheWithDate(Map<K1, Map<K2, CacheEntry<V>>> cache) {
        cache.forEach((email, dateMap) -> {
            dateMap.entrySet().removeIf(entry -> entry.getValue().isExpired());
            if (dateMap.isEmpty()) {
                cache.remove(email);
            }
        });
    }

    private <K, V> void cleanSimpleCache(Map<K, CacheEntry<V>> cache) {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public void putIntakesWithDate(String email, LocalDate date, List<DailyIntake> intakes) {
        if (email == null || date == null || intakes == null) {
            logger.warn("Попытка добавить данные с null параметрами");
            return;
        }
        var dateCache = intakesWithDateCache.computeIfAbsent(email, k -> new ConcurrentHashMap<>());
        dateCache.put(date, new CacheEntry<>(Collections.unmodifiableList(new ArrayList<>(intakes)), INTAKES_WITH_DATE_TTL));
        enforceLimit(dateCache, INTAKES_WITH_DATE_LIMIT);
    }

    public Optional<List<DailyIntake>> getIntakesWithDate(String email, LocalDate date) {
        if (email == null || date == null) {
            logger.warn("Попытка получить данные с null параметрами: email={}, date={}", email, date);
            return Optional.empty();
        }

        Optional<List<DailyIntake>> result = Optional.ofNullable(intakesWithDateCache.get(email))
                .map(map -> map.get(date))
                .filter(entry -> !entry.isExpired())
                .map(CacheEntry::getValue);

        logCacheAccess("с датой", email, date, result);

        return result;
    }

    public Optional<List<DailyIntake>> getIntakesWithoutDate(String email) {
        if (email == null) {
            logger.warn("Попытка получить данные с null email");
            return Optional.empty();
        }

        Optional<List<DailyIntake>> result = Optional.ofNullable(intakesWithoutDateCache.get(email))
                .filter(entry -> !entry.isExpired())
                .map(CacheEntry::getValue);

        logCacheAccess("без даты", email, null, result);
        return result;
    }

    private void logCacheAccess(String cacheType, String email, LocalDate date, Optional<List<DailyIntake>> result) {
        if (result.isPresent()) {
            List<DailyIntake> intakes = result.get();
            String intakeInfo = intakes.stream()
                    .limit(3)
                    .map(i -> String.format(
                            "{id=%d, date=%s, userId=%s}",
                            i.getId(),
                            i.getCreationDate(),
                            i.getUser() != null ? i.getUser().getId() : "null"))
                    .collect(Collectors.joining(", "));

            if (intakes.size() > 3) {
                intakeInfo += ", ... (всего " + intakes.size() + " записей)";
            }

            if (date != null) {
                logger.info("Найдены данные {} для {} на дату {}: {}", cacheType, email, date, intakeInfo);
            } else {
                logger.info("Найдены данные {} для {}: {}", cacheType, email, intakeInfo);
            }
        } else {
            if (date != null) {
                logger.warn("Данные {} для {} на дату {} не найдены", cacheType, email, date);
            } else {
                logger.warn("Данные {} для {} не найдены", cacheType, email);
            }
        }
    }

    public Optional<Map<String, Double>> getNutritionData(String email, LocalDate date) {
        if (email == null || date == null) {
            logger.warn("Попытка получить данные о питательных веществах с null параметрами: email={}, date={}",
                    email, date);
            return Optional.empty();
        }

        Optional<Map<String, Double>> result = Optional.ofNullable(nutritionCache.get(email))
                .map(map -> map.get(date))
                .filter(entry -> !entry.isExpired())
                .map(CacheEntry::getValue);

        if (result.isPresent()) {
            Map<String, Double> nutrition = result.get();
            String example = nutrition.entrySet().stream()
                    .limit(3)
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", "));

            if (nutrition.size() > 3) {
                example += ", ... (всего " + nutrition.size() + " элементов)";
            }

            logger.info("Найдены данные о питательных веществах для {} на {}. {}",
                    email, date, example);
        } else {
            logger.warn("Данные о питательных веществах для {} на {} не найдены", email, date);
        }

        return result;
    }

    public void putIntakesWithoutDate(String email, List<DailyIntake> intakes) {
        if (email == null || intakes == null) {
            logger.warn("Попытка добавить данные с null параметрами");
            return;
        }
        intakesWithoutDateCache.put(
                email,
                new CacheEntry<>(Collections.unmodifiableList(new ArrayList<>(intakes)), INTAKES_WITHOUT_DATE_TTL)
        );
        enforceLimit(intakesWithoutDateCache, INTAKES_WITHOUT_DATE_LIMIT);
    }

    public void putNutritionData(String email, LocalDate date, Map<String, Double> nutrition) {
        if (email == null || date == null || nutrition == null) {
            logger.warn("Попытка добавить данные о питательных веществах с null параметрами");
            return;
        }
        var nutritionMap = nutritionCache.computeIfAbsent(email, k -> new ConcurrentHashMap<>());
        nutritionMap.put(date, new CacheEntry<>(new HashMap<>(nutrition), NUTRITION_DATA_TTL));
    }

    public void evictIntakesWithDate(String email, LocalDate date) {
        if (email != null && date != null) {
            Optional.ofNullable(intakesWithDateCache.get(email))
                    .ifPresent(map -> map.remove(date));
        }
    }

    public void evictNutritionData(String email, LocalDate date) {
        if (email != null && date != null) {
            Optional.ofNullable(nutritionCache.get(email))
                    .ifPresent(map -> map.remove(date));
        }
    }

    private <K, V> void enforceLimit(Map<K, CacheEntry<V>> cache, int limit) {
        if (cache.size() > limit) {
            cache.clear();
        }
    }

    @EventListener
    public void handleDailyIntakeDeleted(DailyIntakeDeletedEvent event) {
        evictIntakesWithDate(event.getEmail(), event.getDate());
        evictNutritionData(event.getEmail(), event.getDate());
    }
}