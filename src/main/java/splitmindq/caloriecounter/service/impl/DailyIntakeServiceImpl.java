package splitmindq.caloriecounter.service.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.dao.DailyIntakeRepository;
import splitmindq.caloriecounter.dao.FoodRepository;
import splitmindq.caloriecounter.dao.UserRepository;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.DailyIntake;
import splitmindq.caloriecounter.model.Food;
import splitmindq.caloriecounter.model.User;
import splitmindq.caloriecounter.service.DailyIntakeService;

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Food> foods = foodRepository.findAllById(foodIds);
        if (foods.isEmpty()) {
            throw new ResourceNotFoundException("Food not found with id: " + foodIds);
        }

        DailyIntake dailyIntake = new DailyIntake();
        dailyIntake.setUser(user);
        dailyIntake.setFoods(foods);

        dailyIntakeRepository.save(dailyIntake);
    }

    @Override
    public DailyIntake getDailyIntakeById(Long id) {
        return dailyIntakeRepository.findById(id).orElse(null);
    }

    @Override
    public void updateDailyIntake(Long id, DailyIntake updateDailyIntake) {
        DailyIntake existingDailyIntake = dailyIntakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DailyIntake not found with id: " + id));

        existingDailyIntake.setCreationDate(updateDailyIntake.getCreationDate());

        if (updateDailyIntake.getFoods() != null) {
            existingDailyIntake.setFoods(updateDailyIntake.getFoods());
        }
        dailyIntakeRepository.save(existingDailyIntake);
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
