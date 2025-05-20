package splitmindq.caloriecounter.service;

import java.time.LocalDate;
import java.util.List;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import splitmindq.caloriecounter.cache.DailyIntakeCache;
import splitmindq.caloriecounter.dao.DailyIntakeFoodRepository;
import splitmindq.caloriecounter.dao.DailyIntakeRepository;
import splitmindq.caloriecounter.dao.FoodRepository;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.DailyIntakeFood;
import splitmindq.caloriecounter.model.Food;
import splitmindq.caloriecounter.service.FoodService;

@Primary
@Service
@AllArgsConstructor
public class FoodServiceImpl implements FoodService {
    private final FoodRepository foodRepository;
    private final DailyIntakeFoodRepository dailyIntakeFoodRepository;
    private final DailyIntakeCache dailyIntakeCache;
    private final DailyIntakeRepository dailyIntakeRepository;

    @Override
    public void createFood(Food food) {
        foodRepository.save(food);
    }

    @Override
    public Food getFoodById(Long id) {
        return foodRepository.findById(id).orElse(null);
    }

    @Override
    public List<Food> getAllFood() {
        return foodRepository.findAll();
    }

    @Override
    public void updateFood(Long id, Food updatedFood) {
        Food existingFood = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + id));

        if (!existingFood.getName().equals(updatedFood.getName()) &&
            foodRepository.existsByName(updatedFood.getName())) {
            throw new DataIntegrityViolationException("Food with this name already exists.");
        }

        existingFood.setName(updatedFood.getName());
        existingFood.setCalories(updatedFood.getCalories());
        existingFood.setProtein(updatedFood.getProtein());
        existingFood.setFats(updatedFood.getFats());
        existingFood.setCarbs(updatedFood.getCarbs());

        foodRepository.save(existingFood);
    }

    @Override
    @Transactional
    public boolean deleteFood(Long id) {
        try {
            // 1. Находим все записи DailyIntakeFood, связанные с продуктом
            List<DailyIntakeFood> dailyIntakeFoods = dailyIntakeFoodRepository.findByFoodId(id);
            if (!dailyIntakeFoods.isEmpty()) {
                // 2. Очищаем кэш для всех затронутых DailyIntake
                dailyIntakeFoods.stream()
                        .map(DailyIntakeFood::getDailyIntake)
                        .distinct()
                        .forEach(dailyIntake -> {
                            String email = dailyIntake.getUser().getEmail();
                            LocalDate date = dailyIntake.getCreationDate();
                            dailyIntakeCache.evictIntakesWithDate(email, date);
                            dailyIntakeCache.evictNutritionData(email, date);
                            dailyIntakeCache.evictIntakesWithoutDate(email);
                            dailyIntakeCache.evictNutritionDataForIntake(dailyIntake.getId());
                        });

                // 3. Удаляем все записи DailyIntakeFood, связанные с продуктом
                dailyIntakeFoodRepository.deleteAll(dailyIntakeFoods);

                // 4. Проверяем и удаляем пустые DailyIntake
                dailyIntakeFoods.stream()
                        .map(DailyIntakeFood::getDailyIntake)
                        .distinct()
                        .forEach(intake -> {
                            // Проверяем, остались ли связанные DailyIntakeFood
                            List<DailyIntakeFood> remainingFoods = dailyIntakeFoodRepository.findByDailyIntakeId(intake.getId());
                            if (remainingFoods.isEmpty()) {
                                dailyIntakeRepository.delete(intake);
                            }
                        });
            }

            // 5. Удаляем сам продукт
            foodRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нельзя удалить продукт, который связан с дневными рационами.", e);
        }
    }
}