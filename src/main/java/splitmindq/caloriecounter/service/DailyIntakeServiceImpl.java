package splitmindq.caloriecounter.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Nullable;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.cache.DailyIntakeCache;
import splitmindq.caloriecounter.dao.DailyIntakeRepository;
import splitmindq.caloriecounter.dao.FoodRepository;
import splitmindq.caloriecounter.dao.UserRepository;
import splitmindq.caloriecounter.dto.DailyNutritionDto;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.*;
import splitmindq.caloriecounter.requests.DailyIntakeRequest;
import splitmindq.caloriecounter.requests.UpdateDailyIntakeRequest;

@Slf4j
@Service
@AllArgsConstructor
public class DailyIntakeServiceImpl implements DailyIntakeService {
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final DailyIntakeRepository dailyIntakeRepository;
    private final DailyIntakeCache dailyIntakeCache;

    @Override
    public List<DailyIntake> getAllDailyIntakes() {
        return dailyIntakeRepository.findAll();
    }

    @Override
    @Transactional
    public DailyIntake addFoodToDailyIntake(Long dailyIntakeId, Long foodId, double weight) {
        // 1. Находим сущности
        DailyIntake dailyIntake = dailyIntakeRepository.findById(dailyIntakeId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyIntake not found"));

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found"));

        // 2. Обновляем данные
        Optional<DailyIntakeFood> existingEntry = dailyIntake.getDailyIntakeFoods().stream()
                .filter(entry -> entry.getFood().getId().equals(foodId))
                .findFirst();

        if (existingEntry.isPresent()) {
            existingEntry.get().setWeight(existingEntry.get().getWeight() + weight);
        } else {
            DailyIntakeFood newEntry = new DailyIntakeFood();
            newEntry.setDailyIntake(dailyIntake);
            newEntry.setFood(food);
            newEntry.setWeight(weight);
            dailyIntake.getDailyIntakeFoods().add(newEntry);
        }

        evictDailyIntakeCache(dailyIntake);

        return dailyIntakeRepository.save(dailyIntake);
    }

    private void evictDailyIntakeCache(DailyIntake dailyIntake) {
        String email = dailyIntake.getUser().getEmail();
        LocalDate date = dailyIntake.getCreationDate();

        dailyIntakeCache.evictIntakesWithDate(email, date);
        dailyIntakeCache.evictNutritionData(email, date);
    }

    @Override
    public DailyIntake createDailyIntake(DailyIntakeRequest dailyIntakeRequest) {
        // Находим пользователя
        User user = userRepository.findById(dailyIntakeRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        DailyIntake dailyIntake = new DailyIntake();
        dailyIntake.setUser(user);

        Map<Long, DailyIntakeFood> foodMap = new HashMap<>();

        for (FoodEntry foodEntry : dailyIntakeRequest.getFoodEntries()) {
            Food food = foodRepository.findById(foodEntry.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Food not found"));

            DailyIntakeFood existingEntry = foodMap.get(foodEntry.getFoodId());

            if (existingEntry != null) {
                existingEntry.setWeight(existingEntry.getWeight() + foodEntry.getWeight());
            } else {
                DailyIntakeFood dailyIntakeFood = new DailyIntakeFood();
                dailyIntakeFood.setDailyIntake(dailyIntake);
                dailyIntakeFood.setFood(food);
                dailyIntakeFood.setWeight(foodEntry.getWeight());

                foodMap.put(foodEntry.getFoodId(), dailyIntakeFood);
                dailyIntake.getDailyIntakeFoods().add(dailyIntakeFood);
            }
        }

        dailyIntakeRepository.save(dailyIntake);
        return dailyIntake;
    }

    @Override
    public DailyIntake getDailyIntakeById(Long id) {
        return dailyIntakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DailyIntake not found with id: " + id));
    }

    @Override
    public void updateDailyIntake(Long id, UpdateDailyIntakeRequest request)
            throws ResourceNotFoundException {
        DailyIntake dailyIntake = dailyIntakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DailyIntake not found"));

        Map<Long, DailyIntakeFood> foodMap = new HashMap<>();
        for (DailyIntakeFood dailyIntakeFood : dailyIntake.getDailyIntakeFoods()) {
            foodMap.put(dailyIntakeFood.getFood().getId(), dailyIntakeFood);
        }

        for (int i = 0; i < request.getFoodIds().size(); i++) {
            Long foodId = request.getFoodIds().get(i);
            double weight = request.getWeights().get(i);

            if (foodMap.containsKey(foodId)) {
                DailyIntakeFood existingEntry = foodMap.get(foodId);
                existingEntry.setWeight(weight);
            } else {
                Food food = foodRepository.findById(foodId)
                        .orElseThrow(() -> new ResourceNotFoundException("Food not found"));

                DailyIntakeFood dailyIntakeFood = new DailyIntakeFood();
                dailyIntakeFood.setDailyIntake(dailyIntake);
                dailyIntakeFood.setFood(food);
                dailyIntakeFood.setWeight(weight);

                dailyIntake.getDailyIntakeFoods().add(dailyIntakeFood);
            }
        }
        dailyIntakeRepository.save(dailyIntake);
        evictDailyIntakeCache(dailyIntake); // Очищаем кэш
    }

    @Override
    @Transactional
    public boolean deleteDailyIntake(Long id) {
        return dailyIntakeRepository.findById(id)
                .map(dailyIntake -> {
                    String email = dailyIntake.getUser().getEmail();
                    LocalDate date = dailyIntake.getCreationDate();

                    dailyIntakeRepository.delete(dailyIntake);

                    dailyIntakeCache.evictIntakesWithDate(email, date);
                    dailyIntakeCache.evictNutritionData(email, date);

                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyIntake> getUserIntakes(String email, @Nullable LocalDate date) {
        if (date != null) {
            return dailyIntakeCache.getIntakesWithDate(email, date)
                    .orElseGet(() -> {
                        List<DailyIntake> intakes = dailyIntakeRepository.findUserIntakesWithDate(email, date);
                        dailyIntakeCache.putIntakesWithDate(email, date, intakes);
                        return intakes;
                    });
        }
        return dailyIntakeCache.getIntakesWithoutDate(email)
                .orElseGet(() -> {
                    List<DailyIntake> intakes = dailyIntakeRepository.findUserIntakesWithoutDate(email);
                    dailyIntakeCache.putIntakesWithoutDate(email, intakes);
                    return intakes;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public DailyNutritionDto getDailyNutrition(String email, LocalDate date) {
        return dailyIntakeCache.getNutritionData(email, date)
                .map(DailyNutritionDto::new)
                .orElseGet(() -> {
                    Map<String, Double> nutrition = dailyIntakeRepository.calculateDailyNutrition(email, date)
                            .orElseThrow(() -> new ResourceNotFoundException("Nutrition data not found"));
                    dailyIntakeCache.putNutritionData(email, date, nutrition);
                    return new DailyNutritionDto(nutrition);
                });
    }

}