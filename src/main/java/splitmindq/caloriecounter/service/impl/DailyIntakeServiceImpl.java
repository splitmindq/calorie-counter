package splitmindq.caloriecounter.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.dao.DailyIntakeRepository;
import splitmindq.caloriecounter.dao.FoodRepository;
import splitmindq.caloriecounter.dao.UserRepository;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.*;
import splitmindq.caloriecounter.requests.DailyIntakeRequest;
import splitmindq.caloriecounter.requests.UpdateDailyIntakeRequest;
import splitmindq.caloriecounter.service.DailyIntakeService;

@Service
@AllArgsConstructor
public class DailyIntakeServiceImpl implements DailyIntakeService {
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final DailyIntakeRepository dailyIntakeRepository;

    @Override
    public List<DailyIntake> getAllDailyIntakes() {
        return dailyIntakeRepository.findAll();
    }

    @Override
    public DailyIntake addFoodToDailyIntake(Long dailyIntakeId, Long foodId, double weight) {
        // Находим прием пищи
        DailyIntake dailyIntake = dailyIntakeRepository.findById(dailyIntakeId)
                .orElseThrow(() -> new RuntimeException("DailyIntake not found"));

        // Находим продукт
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found"));

        // Проверяем, есть ли уже такой продукт в приеме пищи
        Optional<DailyIntakeFood> existingEntry = dailyIntake.getDailyIntakeFoods().stream()
                .filter(entry -> entry.getFood().getId().equals(foodId))
                .findFirst();

        if (existingEntry.isPresent()) {
            // Если продукт уже есть, увеличиваем его вес
            DailyIntakeFood dailyIntakeFood = existingEntry.get();
            dailyIntakeFood.setWeight(dailyIntakeFood.getWeight() + weight);
        } else {
            // Если продукта нет, создаем новую запись
            DailyIntakeFood dailyIntakeFood = new DailyIntakeFood();
            dailyIntakeFood.setDailyIntake(dailyIntake);
            dailyIntakeFood.setFood(food);
            dailyIntakeFood.setWeight(weight);

            // Добавляем в список
            dailyIntake.getDailyIntakeFoods().add(dailyIntakeFood);
        }

        // Сохраняем изменения
        return dailyIntakeRepository.save(dailyIntake);
    }

    @Override
    public void createDailyIntake(DailyIntakeRequest dailyIntakeRequest) {
        // Находим пользователя
        User user = userRepository.findById(dailyIntakeRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Создаем новый прием пищи
        DailyIntake dailyIntake = new DailyIntake();
        dailyIntake.setUser(user);

        // Используем Map для быстрого поиска продуктов по foodId
        Map<Long, DailyIntakeFood> foodMap = new HashMap<>();

        // Обрабатываем каждый FoodEntry
        for (FoodEntry foodEntry : dailyIntakeRequest.getFoodEntries()) {
            // Находим продукт
            Food food = foodRepository.findById(foodEntry.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Food not found"));

            // Проверяем, есть ли уже такой продукт в dailyIntake
            DailyIntakeFood existingEntry = foodMap.get(foodEntry.getFoodId());

            if (existingEntry != null) {
                // Если продукт уже есть, увеличиваем его вес
                existingEntry.setWeight(existingEntry.getWeight() + foodEntry.getWeight());
            } else {
                // Если продукта нет, создаем новую запись
                DailyIntakeFood dailyIntakeFood = new DailyIntakeFood();
                dailyIntakeFood.setDailyIntake(dailyIntake);
                dailyIntakeFood.setFood(food);
                dailyIntakeFood.setWeight(foodEntry.getWeight());

                // Добавляем в Map и в список
                foodMap.put(foodEntry.getFoodId(), dailyIntakeFood);
                dailyIntake.getDailyIntakeFoods().add(dailyIntakeFood);
            }
        }

        // Сохраняем прием пищи
        dailyIntakeRepository.save(dailyIntake);
    }

    @Override
    public DailyIntake getDailyIntakeById(Long id) {
        return dailyIntakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DailyIntake not found with id: " + id));
    }

    @Override
    public void updateDailyIntake(Long id, UpdateDailyIntakeRequest request) throws ResourceNotFoundException {
        // Находим прием пищи
        DailyIntake dailyIntake = dailyIntakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DailyIntake not found"));

        // Создаем Map для быстрого поиска продуктов по их ID
        Map<Long, DailyIntakeFood> foodMap = new HashMap<>();
        for (DailyIntakeFood dailyIntakeFood : dailyIntake.getDailyIntakeFoods()) {
            foodMap.put(dailyIntakeFood.getFood().getId(), dailyIntakeFood);
        }

        // Обрабатываем каждый продукт из запроса
        for (int i = 0; i < request.getFoodIds().size(); i++) {
            Long foodId = request.getFoodIds().get(i);
            double weight = request.getWeights().get(i);

            // Проверяем, есть ли уже такой продукт в приеме пищи
            if (foodMap.containsKey(foodId)) {
                // Если продукт уже есть, обновляем его вес
                DailyIntakeFood existingEntry = foodMap.get(foodId);
                existingEntry.setWeight(weight);
            } else {
                // Если продукта нет, создаем новую запись
                Food food = foodRepository.findById(foodId)
                        .orElseThrow(() -> new ResourceNotFoundException("Food not found"));

                DailyIntakeFood dailyIntakeFood = new DailyIntakeFood();
                dailyIntakeFood.setDailyIntake(dailyIntake);
                dailyIntakeFood.setFood(food);
                dailyIntakeFood.setWeight(weight);

                // Добавляем в список
                dailyIntake.getDailyIntakeFoods().add(dailyIntakeFood);
            }
        }

        // Сохраняем изменения
        dailyIntakeRepository.save(dailyIntake);
    }

    @Override
    public boolean deleteDailyIntake(Long id) {
        if (dailyIntakeRepository.existsById(id)) {
            dailyIntakeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}