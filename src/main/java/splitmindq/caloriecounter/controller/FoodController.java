package splitmindq.caloriecounter.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
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
    public Food getFoodById(@PathVariable Long id) {
        return foodService.getFoodById(id);
    }

    @PutMapping("update_food/{id}")
    public ResponseEntity<String> updateFood(@PathVariable Long id, @RequestBody Food food) {
        try {
            foodService.updateFood(id, food);
            return ResponseEntity.status(HttpStatus.OK).body("User updated successfully.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("delete_food/{id}")
    public void deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
    }
}
