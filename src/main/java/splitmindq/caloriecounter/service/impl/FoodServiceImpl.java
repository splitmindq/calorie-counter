package splitmindq.caloriecounter.service.impl;

import java.time.LocalDate;
import java.util.List;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.cache.DailyIntakeCache;
import splitmindq.caloriecounter.dao.DailyIntakeFoodRepository;
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
        List<DailyIntakeFood> dailyIntakeFoods = dailyIntakeFoodRepository.findByFoodId(id);

        dailyIntakeFoods.stream()
                .map(DailyIntakeFood::getDailyIntake)
                .distinct() // Убираем дубликаты
                .forEach(dailyIntake -> {
                    String email = dailyIntake.getUser().getEmail();
                    LocalDate date = dailyIntake.getCreationDate();
                    dailyIntakeCache.evictIntakesWithDate(email, date);
                    dailyIntakeCache.evictNutritionData(email, date);
                });

        // 4. Удаляем связи и саму еду
        dailyIntakeFoodRepository.deleteAll(dailyIntakeFoods);
        foodRepository.deleteById(id);

        return true;
    }
}
