package splitmindq.caloriecounter.service;

import java.util.List;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.model.Food;

@Service
public interface FoodService {
    void createFood(Food food);

    Food getFoodById(Long id);

    List<Food> getAllFood();

    void updateFood(Long id, Food food);

    boolean deleteFood(Long id);
}
