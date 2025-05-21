package splitmindq.caloriecounter.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.Nullable;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.cache.DailyIntakeCache;
import splitmindq.caloriecounter.dao.DailyIntakeRepository;
import splitmindq.caloriecounter.dao.FoodRepository;
import splitmindq.caloriecounter.dao.UserRepository;
import splitmindq.caloriecounter.dto.DailyNutritionDto;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.listener.DailyIntakeDeletedEvent;
import splitmindq.caloriecounter.model.*;
import splitmindq.caloriecounter.requests.DailyIntakeRequest;
import splitmindq.caloriecounter.requests.UpdateDailyIntakeRequest;

@Slf4j
@Service
@AllArgsConstructor
public class DailyIntakeServiceImpl implements DailyIntakeService {
    private static final String FOOD_NOT_FOUND_MSG = "Food not found";
    private static final String DAILY_INTAKE_NOT_FOUND_MSG = "DailyIntake not found";
    private static final String USER_NOT_FOUND_MSG = "User not found";

    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final DailyIntakeRepository dailyIntakeRepository;
    private final DailyIntakeCache dailyIntakeCache;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<DailyIntake> getAllDailyIntakes() {
        List<DailyIntake> intakes = dailyIntakeRepository.findAll();
        log.info("Retrieved {} daily intakes", intakes.size());
        return intakes;
    }

    @Override
    @Transactional
    public DailyIntake addFoodToDailyIntake(Long dailyIntakeId, Long foodId, double weight) {
        DailyIntake dailyIntake = dailyIntakeRepository.findById(dailyIntakeId)
                .orElseThrow(() -> new ResourceNotFoundException(DAILY_INTAKE_NOT_FOUND_MSG));
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException(FOOD_NOT_FOUND_MSG));

        dailyIntake.getDailyIntakeFoods().stream()
                .filter(entry -> entry.getFood().getId().equals(foodId))
                .findFirst()
                .ifPresentOrElse(
                        entry -> entry.setWeight(entry.getWeight() + weight),
                        () -> {
                            DailyIntakeFood newEntry = new DailyIntakeFood();
                            newEntry.setDailyIntake(dailyIntake);
                            newEntry.setFood(food);
                            newEntry.setWeight(weight);
                            dailyIntake.getDailyIntakeFoods().add(newEntry);
                        }
                );

        evictDailyIntakeCache(dailyIntake);
        DailyIntake savedIntake = dailyIntakeRepository.save(dailyIntake);
        log.info("Added foodId={} with weight={} to dailyIntakeId={}", foodId, weight, dailyIntakeId);
        return savedIntake;
    }

    private void evictDailyIntakeCache(DailyIntake dailyIntake) {
        if (dailyIntake.getUser() != null) {
            String email = dailyIntake.getUser().getEmail();
            LocalDate date = dailyIntake.getCreationDate();
            if (date != null) {
                dailyIntakeCache.evictIntakesWithDate(email, date);
                dailyIntakeCache.evictNutritionData(email, date);
                log.info("Evicted cache for email={} and date={}", email, date);
            }
            dailyIntakeCache.evictIntakesWithoutDate(email);
            dailyIntakeCache.evictNutritionDataForIntake(dailyIntake.getId());
            log.info("Evicted intakesWithoutDate and intakeNutrition cache for email={} and intakeId={}",
                    email, dailyIntake.getId());
        }
    }

    @Override
    @Transactional
    public DailyIntake createDailyIntake(DailyIntakeRequest dailyIntakeRequest) {
        User user = userRepository.findById(dailyIntakeRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG));

        dailyIntakeCache.evictIntakesWithoutDate(user.getEmail());
        dailyIntakeCache.evictNutritionData(user.getEmail(), LocalDate.now());

        DailyIntake dailyIntake = new DailyIntake();
        dailyIntake.setUser(user);
        dailyIntake.setCreationDate(LocalDate.now());

        // Объединяем дублирующиеся foodId
        Map<Long, Double> foodWeightMap = new HashMap<>();
        dailyIntakeRequest.getFoodEntries().forEach(foodEntry -> {
            foodWeightMap.merge(foodEntry.getFoodId(), foodEntry.getWeight(), Double::sum);
        });

        foodWeightMap.forEach((foodId, weight) -> {
            Food food = foodRepository.findById(foodId)
                    .orElseThrow(() -> new ResourceNotFoundException(FOOD_NOT_FOUND_MSG));
            DailyIntakeFood dailyIntakeFood = new DailyIntakeFood();
            dailyIntakeFood.setDailyIntake(dailyIntake);
            dailyIntakeFood.setFood(food);
            dailyIntakeFood.setWeight(weight);
            dailyIntake.getDailyIntakeFoods().add(dailyIntakeFood);
        });

        DailyIntake savedIntake = dailyIntakeRepository.save(dailyIntake);
        evictDailyIntakeCache(savedIntake);
        log.info("Created daily intake with id={} for userId={}, food count={}",
                savedIntake.getId(), user.getId(), savedIntake.getDailyIntakeFoods().size());
        return savedIntake;
    }

    @Override
    @Transactional(readOnly = true)
    public DailyIntake getDailyIntakeById(Long id) {
        DailyIntake intake = dailyIntakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DAILY_INTAKE_NOT_FOUND_MSG + " with id: " + id));
        log.info("Retrieved daily intake with id={}", id);
        return intake;
    }
    @Transactional
    public void updateDailyIntake(Long id, UpdateDailyIntakeRequest request) {
        DailyIntake dailyIntake = dailyIntakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DAILY_INTAKE_NOT_FOUND_MSG));

        log.info("Updating daily intake id={} with foodIds={} and weights={}",
                id, request.getFoodIds(), request.getWeights());

        // Логируем текущие продукты
        log.info("Current foods in daily intake id={}: {}", id,
                dailyIntake.getDailyIntakeFoods().stream()
                        .map(dif -> "foodId=" + dif.getFood().getId() + ", weight=" + dif.getWeight())
                        .collect(Collectors.joining("; ")));

        // Создаем карту новых продуктов из запроса: foodId -> weight
        Map<Long, Double> newFoodWeightMap = new HashMap<>();
        for (int i = 0; i < request.getFoodIds().size(); i++) {
            newFoodWeightMap.merge(request.getFoodIds().get(i), request.getWeights().get(i), Double::sum);
        }
        log.info("New food weight map: {}", newFoodWeightMap);

        // Подсчитываем количество продуктов до удаления
        int initialSize = dailyIntake.getDailyIntakeFoods().size();

        // Удаляем продукты, которых нет в новом запросе
        dailyIntake.getDailyIntakeFoods().removeIf(dif -> {
            boolean shouldRemove = !newFoodWeightMap.containsKey(dif.getFood().getId());
            if (shouldRemove) {
                log.info("Removing foodId={} from daily intake id={}", dif.getFood().getId(), id);
            }
            return shouldRemove;
        });

        // Подсчитываем количество удаленных продуктов
        int removedCount = initialSize - dailyIntake.getDailyIntakeFoods().size();
        log.info("Removed {} foods from daily intake id={}", removedCount, id);

        // Создаем карту текущих продуктов для быстрого доступа
        Map<Long, DailyIntakeFood> currentFoods = dailyIntake.getDailyIntakeFoods().stream()
                .collect(Collectors.toMap(
                        dif -> dif.getFood().getId(),
                        dif -> dif,
                        (existing, replacement) -> existing // Если есть дубликаты, оставляем первый
                ));

        // Обновляем существующие продукты и добавляем новые
        newFoodWeightMap.forEach((foodId, weight) -> {
            DailyIntakeFood existingFood = currentFoods.get(foodId);
            if (existingFood != null) {
                // Обновляем вес существующего продукта
                log.info("Updating weight for foodId={} to {}", foodId, weight);
                existingFood.setWeight(weight);
            } else {
                // Добавляем новый продукт
                log.info("Adding new foodId={} with weight={} to daily intake id={}", foodId, weight, id);
                Food food = foodRepository.findById(foodId)
                        .orElseThrow(() -> new ResourceNotFoundException(FOOD_NOT_FOUND_MSG));
                DailyIntakeFood newDailyIntakeFood = new DailyIntakeFood();
                newDailyIntakeFood.setDailyIntake(dailyIntake);
                newDailyIntakeFood.setFood(food);
                newDailyIntakeFood.setWeight(weight);
                dailyIntake.getDailyIntakeFoods().add(newDailyIntakeFood);
            }
        });

        // Сохраняем обновленный рацион
        log.info("Saving daily intake id={} with {} foods", id, dailyIntake.getDailyIntakeFoods().size());
        dailyIntakeRepository.save(dailyIntake);

        // Инвалидируем и обновляем кэш
        String email = dailyIntake.getUser() != null ? dailyIntake.getUser().getEmail() : null;
        LocalDate date = dailyIntake.getCreationDate();
        if (email != null && date != null) {
            // Инвалидируем старые данные
            evictDailyIntakeCache(dailyIntake);

            // Обновляем intakesWithDateCache
            dailyIntakeCache.putIntakesWithDate(email, date, List.of(dailyIntake));

            // Инвалидируем nutritionCache и intakeNutritionCache
            dailyIntakeCache.evictNutritionData(email, date);
            dailyIntakeCache.evictNutritionDataForIntake(id);

            log.info("Updated cache for daily intake id={}, email={}, date={}", id, email, date);
        } else {
            log.warn("Cannot update cache: email={} or date={} is null for daily intake id={}", email, date, id);
        }

        log.info("Updated daily intake id={}. Food count: {}", id, dailyIntake.getDailyIntakeFoods().size());
    }


    // Предполагается, что метод evictDailyIntakeCache реализован где-то в сервисе


    @Override
    @Transactional
    public boolean deleteDailyIntake(Long id) {
        return dailyIntakeRepository.findById(id)
                .map(dailyIntake -> {
                    String email = dailyIntake.getUser() != null ? dailyIntake.getUser().getEmail() : null;
                    LocalDate date = dailyIntake.getCreationDate();
                    Long intakeId = dailyIntake.getId();

                    evictDailyIntakeCache(dailyIntake);

                    if (email != null && date != null) {
                        dailyIntakeCache.evictNutritionData(email, date);
                        eventPublisher.publishEvent(new DailyIntakeDeletedEvent(this, email, date, intakeId));
                    }

                    dailyIntakeRepository.delete(dailyIntake);
                    log.info("Deleted daily intake with id={}", id);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("Failed to delete daily intake with id={} - not found", id);
                    return false;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyIntake> getUserIntakes(String email, @Nullable LocalDate date) {
        if (date != null) {
            return dailyIntakeCache.getIntakesWithDate(email, date)
                    .orElseGet(() -> {
                        List<DailyIntake> intakes = dailyIntakeRepository.findUserIntakesWithDate(email, date);
                        dailyIntakeCache.putIntakesWithDate(email, date, intakes);
                        log.info("Retrieved {} intakes for email={} and date={}", intakes.size(), email, date);
                        return intakes;
                    });
        }
        // Убрана принудительная очистка кэша, полагаемся на TTL
        return dailyIntakeCache.getIntakesWithoutDate(email)
                .orElseGet(() -> {
                    List<DailyIntake> intakes = dailyIntakeRepository.findUserIntakesWithoutDate(email);
                    dailyIntakeCache.putIntakesWithoutDate(email, intakes);
                    log.info("Retrieved {} intakes for email={} without date", intakes.size(), email);
                    return intakes;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public DailyNutritionDto getDailyNutrition(String email, LocalDate date) {
        return dailyIntakeCache.getNutritionData(email, date)
                .map(nutrition -> {
                    log.info("Retrieved cached nutrition data for email={} and date={}: {}", email, date, nutrition);
                    return new DailyNutritionDto(nutrition);
                })
                .orElseGet(() -> {
                    Optional<Map<String, Double>> nutritionOpt = dailyIntakeRepository.calculateDailyNutrition(email, date);
                    if (nutritionOpt.isEmpty() || nutritionOpt.get().values().stream().allMatch(v -> v == null || v == 0.0)) {
                        log.warn("Nutrition data not found or all values are zero for email={} and date={}", email, date);
                        return null;
                    }
                    Map<String, Double> nutrition = nutritionOpt.get();
                    dailyIntakeCache.putNutritionData(email, date, nutrition);
                    log.info("Calculated nutrition data for email={} and date={}: {}", email, date, nutrition);
                    return new DailyNutritionDto(nutrition);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public DailyNutritionDto getNutritionForIntake(Long intakeId) {
        Optional<Map<String, Double>> cachedNutrition = dailyIntakeCache.getNutritionDataForIntake(intakeId);
        if (cachedNutrition.isPresent()) {
            log.info("Retrieved cached nutrition data for intakeId={}: {}", intakeId, cachedNutrition.get());
            return new DailyNutritionDto(cachedNutrition.get());
        }

        Optional<Map<String, Double>> nutritionOpt = dailyIntakeRepository.calculateNutritionForIntake(intakeId);
        if (nutritionOpt.isEmpty() || nutritionOpt.get().values().stream().allMatch(v -> v == null || v == 0.0)) {
            log.warn("Nutrition data not found or all values are zero for intakeId={}", intakeId);
            return new DailyNutritionDto(0.0, 0.0, 0.0, 0.0);
        }
        Map<String, Double> nutrition = nutritionOpt.get();
        dailyIntakeCache.putNutritionDataForIntake(intakeId, nutrition);
        log.info("Calculated nutrition data for intakeId={}: {}", intakeId, nutrition);
        return new DailyNutritionDto(nutrition);
    }
}