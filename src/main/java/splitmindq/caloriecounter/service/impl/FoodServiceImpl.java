package splitmindq.caloriecounter.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.dao.FoodRepository;
import splitmindq.caloriecounter.model.Food;
import splitmindq.caloriecounter.service.FoodService;

import java.util.List;

@Primary
@Service
@AllArgsConstructor
public class FoodServiceImpl implements FoodService {
    private final FoodRepository foodRepository;

    @Override
    public void createFood(Food food) {
        foodRepository.save(food);
    }

    @Override
    public Food getFood(Long id) {
        return foodRepository.findById(id).orElse(null);
    }

    @Override
    public List<Food> getAllFood() {
        return foodRepository.findAll();
    }

    @Override
    public void updateFood(Food food) {
        foodRepository.save(food);
    }

    @Override
    public void deleteFood(Long id) {
        foodRepository.deleteById(id);
    }
}
