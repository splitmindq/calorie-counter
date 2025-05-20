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
    private static final long INTAKES_WITH_DATE_TTL = 10; // 10 минут
    private static final long INTAKES_WITHOUT_DATE_TTL = 10; // 10 минут
    private static final long NUTRITION_DATA_TTL = 30; // 30 минут
    private static final long INTAKE_NUTRITION_TTL = 30; // 30 минут

    private static final int INTAKES_WITH_DATE_LIMIT = 100;
    private static final int INTAKES_WITHOUT_DATE_LIMIT = 100;
    private static final int NUTRITION_LIMIT = 100;
    private static final int INTAKE_NUTRITION_LIMIT = 100;

    private final Map<String, Map<LocalDate, CacheEntry<List<DailyIntake>>>> intakesWithDateCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<List<DailyIntake>>> intakesWithoutDateCache = new ConcurrentHashMap<>();
    private final Map<String, Map<LocalDate, CacheEntry<Map<String, Double>>>> nutritionCache = new ConcurrentHashMap<>();
    private final Map<Long, CacheEntry<Map<String, Double>>> intakeNutritionCache = new ConcurrentHashMap<>();

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
        cleanSimpleCache(intakeNutritionCache);
        logger.debug("Performed cache cleanup");
    }

    private <K1, K2, V> void cleanCacheWithDate(Map<K1, Map<K2, CacheEntry<V>>> cache) {
        cache.forEach((key1, dateMap) -> {
            dateMap.entrySet().removeIf(entry -> entry.getValue().isExpired());
            if (dateMap.isEmpty()) {
                cache.remove(key1);
            }
        });
    }

    private <K, V> void cleanSimpleCache(Map<K, CacheEntry<V>> cache) {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public void putIntakesWithDate(String email, LocalDate date, List<DailyIntake> intakes) {
        if (email == null || date == null || intakes == null) {
            logger.warn("Attempt to cache null data: email={}, date={}", email, date);
            return;
        }

        if (intakes.isEmpty()) {
            logger.info("Not caching empty intake list for email={} and date={}", email, date);
            return;
        }

        var dateCache = intakesWithDateCache.computeIfAbsent(email, k -> new ConcurrentHashMap<>());
        dateCache.put(date, new CacheEntry<>(Collections.unmodifiableList(new ArrayList<>(intakes)), INTAKES_WITH_DATE_TTL));
        enforceLimit(dateCache, INTAKES_WITH_DATE_LIMIT);
        logger.info("Cached {} intakes for email={} and date={}", intakes.size(), email, date);
    }

    public Optional<List<DailyIntake>> getIntakesWithDate(String email, LocalDate date) {
        if (email == null || date == null) {
            logger.warn("Attempt to retrieve data with null parameters: email={}, date={}", email, date);
            return Optional.empty();
        }

        Optional<List<DailyIntake>> result = Optional.ofNullable(intakesWithDateCache.get(email))
                .map(map -> map.get(date))
                .filter(entry -> !entry.isExpired())
                .map(CacheEntry::getValue);

        logCacheAccess("with date", email, date, result);
        return result;
    }

    public Optional<List<DailyIntake>> getIntakesWithoutDate(String email) {
        if (email == null) {
            logger.warn("Attempt to retrieve data with null email");
            return Optional.empty();
        }

        Optional<List<DailyIntake>> result = Optional.ofNullable(intakesWithoutDateCache.get(email))
                .filter(entry -> !entry.isExpired())
                .map(CacheEntry::getValue);

        logCacheAccess("without date", email, null, result);
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
                intakeInfo += ", ... (total " + intakes.size() + " records)";
            }

            if (date != null) {
                logger.info("Found {} data for email={} and date={}: {}", cacheType, email, date, intakeInfo);
            } else {
                logger.info("Found {} data for email={}: {}", cacheType, email, intakeInfo);
            }
        } else {
            if (date != null) {
                logger.debug("No {} data found for email={} and date={}", cacheType, email, date);
            } else {
                logger.debug("No {} data found for email={}", cacheType, email);
            }
        }
    }

    public Optional<Map<String, Double>> getNutritionData(String email, LocalDate date) {
        if (email == null || date == null) {
            logger.warn("Attempt to retrieve nutrition data with null parameters: email={}, date={}", email, date);
            return Optional.empty();
        }

        Optional<Map<String, Double>> result = Optional.ofNullable(nutritionCache.get(email))
                .map(map -> map.get(date))
                .filter(entry -> !entry.isExpired())
                .map(CacheEntry::getValue);

        logNutritionCacheAccess(email, date, result);
        return result;
    }

    public void putNutritionData(String email, LocalDate date, Map<String, Double> nutrition) {
        if (email == null || date == null || nutrition == null) {
            logger.warn("Attempt to cache null nutrition data: email={}, date={}", email, date);
            return;
        }
        var nutritionMap = nutritionCache.computeIfAbsent(email, k -> new ConcurrentHashMap<>());
        nutritionMap.put(date, new CacheEntry<>(new HashMap<>(nutrition), NUTRITION_DATA_TTL));
        enforceLimit(nutritionMap, NUTRITION_LIMIT);
        logger.info("Cached nutrition data for email={} and date={}: {}", email, date, nutrition);
    }

    public Optional<Map<String, Double>> getNutritionDataForIntake(Long intakeId) {
        if (intakeId == null) {
            logger.warn("Attempt to retrieve nutrition data with null intakeId");
            return Optional.empty();
        }

        Optional<Map<String, Double>> result = Optional.ofNullable(intakeNutritionCache.get(intakeId))
                .filter(entry -> !entry.isExpired())
                .map(CacheEntry::getValue);

        if (result.isPresent()) {
            logger.info("Found cached nutrition data for intakeId={}: {}", intakeId, result.get());
        } else {
            logger.debug("No nutrition data found for intakeId={}", intakeId);
        }
        return result;
    }

    public void putNutritionDataForIntake(Long intakeId, Map<String, Double> nutrition) {
        if (intakeId == null || nutrition == null) {
            logger.warn("Attempt to cache null nutrition data for intakeId={}", intakeId);
            return;
        }
        intakeNutritionCache.put(intakeId, new CacheEntry<>(new HashMap<>(nutrition), INTAKE_NUTRITION_TTL));
        enforceLimit(intakeNutritionCache, INTAKE_NUTRITION_LIMIT);
        logger.info("Cached nutrition data for intakeId={}: {}", intakeId, nutrition);
    }

    public void evictNutritionDataForIntake(Long intakeId) {
        if (intakeId != null) {
            intakeNutritionCache.remove(intakeId);
            logger.info("Evicted nutrition cache for intakeId={}", intakeId);
        }
    }

    public void putIntakesWithoutDate(String email, List<DailyIntake> intakes) {
        if (email == null || intakes == null) {
            logger.warn("Attempt to cache null data: email={}", email);
            return;
        }

        if (intakes.isEmpty()) {
            logger.info("Not caching empty intake list for email={}", email);
            return;
        }

        intakesWithoutDateCache.put(
                email,
                new CacheEntry<>(Collections.unmodifiableList(new ArrayList<>(intakes)), INTAKES_WITHOUT_DATE_TTL)
        );
        enforceLimit(intakesWithoutDateCache, INTAKES_WITHOUT_DATE_LIMIT);
        logger.info("Cached {} intakes for email={}", intakes.size(), email);
    }

    public void evictIntakesWithDate(String email, LocalDate date) {
        if (email != null && date != null) {
            Optional.ofNullable(intakesWithDateCache.get(email))
                    .ifPresent(map -> map.remove(date));
            logger.info("Evicted intakesWithDate cache for email={} and date={}", email, date);
        }
    }

    public void evictNutritionData(String email, LocalDate date) {
        if (email != null && date != null) {
            Optional.ofNullable(nutritionCache.get(email))
                    .ifPresent(map -> map.remove(date));
            logger.info("Evicted nutrition cache for email={} and date={}", email, date);
        }
    }

    public void evictIntakesWithoutDate(String email) {
        if (email != null) {
            intakesWithoutDateCache.remove(email);
            logger.info("Evicted intakesWithoutDate cache for email={}", email);
        }
    }

    public void evictAllUserData(String email) {
        intakesWithDateCache.remove(email);
        intakesWithoutDateCache.remove(email);
        nutritionCache.remove(email);
        logger.info("Evicted all cached data for email={}", email);
    }

    private <K, V> void enforceLimit(Map<K, CacheEntry<V>> cache, int limit) {
        if (cache.size() <= limit) {
            return;
        }
        // Сортируем по времени создания и удаляем самые старые записи
        List<Map.Entry<K, CacheEntry<V>>> entries = new ArrayList<>(cache.entrySet());
        entries.sort(Comparator.comparing(e -> e.getValue().getCreatedAt()));
        while (cache.size() > limit && !entries.isEmpty()) {
            cache.remove(entries.remove(0).getKey());
        }
        logger.debug("Enforced cache limit of {} entries, current size={}", limit, cache.size());
    }

    private void logNutritionCacheAccess(String email, LocalDate date, Optional<Map<String, Double>> result) {
        if (result.isPresent()) {
            Map<String, Double> nutrition = result.get();
            String example = nutrition.entrySet().stream()
                    .limit(3)
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", "));
            if (nutrition.size() > 3) {
                example += ", ... (total " + nutrition.size() + " elements)";
            }
            logger.info("Found nutrition data for email={} and date={}: {}", email, date, example);
        } else {
            logger.debug("No nutrition data found for email={} and date={}", email, date);
        }
    }

    @EventListener
    public void handleDailyIntakeDeleted(DailyIntakeDeletedEvent event) {
        evictIntakesWithDate(event.getEmail(), event.getDate());
        evictNutritionData(event.getEmail(), event.getDate());
        evictNutritionDataForIntake(event.getIntakeId());
        logger.info("Handled DailyIntakeDeletedEvent for email={}, date={}, intakeId={}",
                event.getEmail(), event.getDate(), event.getIntakeId());
    }
}