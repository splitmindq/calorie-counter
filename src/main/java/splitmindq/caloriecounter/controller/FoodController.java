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
    public ResponseEntity<List<Food>> getFoods() {
        List<Food> foods = foodService.getAllFood();
        if (foods.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(foods, HttpStatus.OK);
    }

    @PostMapping("create_food")
    public ResponseEntity<String> createFood(@RequestBody Food food) {
        foodService.createFood(food);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Food> getFoodById(@PathVariable Long id) {
        try {
            Food food = foodService.getFoodById(id);
            return new ResponseEntity<>(food, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
    public ResponseEntity<String> deleteFood(@PathVariable Long id) {
        try {
            boolean isDeleted = foodService.deleteFood(id);
            if (isDeleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Food not found.");
            }
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot delete food that is linked to daily intakes.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}