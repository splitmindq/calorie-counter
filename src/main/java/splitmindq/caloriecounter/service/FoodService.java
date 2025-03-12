package splitmindq.caloriecounter.service;

import java.util.List;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.model.Food;

@Service
public interface FoodService {
    void createFood(Food food);

    Food getFoodById(Long id);

    List<Food> getAllFood();

    //    void updateFood(Food food);

    void deleteFood(Long id);
}
