package splitmindq.caloriecounter.controller;

import java.util.List;
import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import splitmindq.caloriecounter.model.Food;
import splitmindq.caloriecounter.service.FoodService;

@RestController
@RequestMapping("/api/v1/foods")
@AllArgsConstructor
public class FoodController {
    private final FoodService foodService;

    @GetMapping
    public List<Food> getFoods() {
        return foodService.getAllFood();
    }

    @PostMapping("create_food")
    public void createFood(@RequestBody Food food) {
        foodService.createFood(food);
    }

    @GetMapping("/{id}")
    public Food getFood(@PathVariable Long id) {
        return foodService.getFood(id);
    }

    @PutMapping("update_food")
    public void updateFood(@RequestBody Food food) {
        foodService.updateFood(food);
    }

    @DeleteMapping("delete_food/{id}")
    public void deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
    }
}
