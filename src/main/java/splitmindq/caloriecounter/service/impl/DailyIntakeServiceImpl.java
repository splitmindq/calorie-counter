package splitmindq.caloriecounter.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.dao.DailyIntakeRepository;
import splitmindq.caloriecounter.dao.FoodRepository;
import splitmindq.caloriecounter.dao.UserRepository;
import splitmindq.caloriecounter.model.DailyIntake;
import splitmindq.caloriecounter.model.User;
import splitmindq.caloriecounter.service.DailyIntakeService;
import splitmindq.caloriecounter.model.Food;

@Primary
@Service
@AllArgsConstructor
public class DailyIntakeServiceImpl implements DailyIntakeService {
    UserRepository userRepository;
    FoodRepository foodRepository;

    private final DailyIntakeRepository dailyIntakeRepository;

    @Override
    public List<DailyIntake> getAllDailyIntakes() {
        return dailyIntakeRepository.findAll();
    }

    @Override
    public void createDailyIntake(Long userId, List<Long> foodIds) {
        // Находим пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Находим продукты
        List<Food> foods = foodRepository.findAllById(foodIds);
        if (foods.isEmpty()) {
            throw new RuntimeException("No foods found with ids: " + foodIds);
        }

        // Создаем новый DailyIntake
        DailyIntake dailyIntake = new DailyIntake();
        dailyIntake.setUser(user); // Устанавливаем пользователя
        dailyIntake.setFoods(foods); // Устанавливаем продукты

        // Сохраняем в базу данных
        dailyIntakeRepository.save(dailyIntake);
    }

    @Override
    public DailyIntake getDailyIntakeById(Long id) {
        return dailyIntakeRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteDailyIntake(Long id) {
        dailyIntakeRepository.deleteById(id);
    }
}
