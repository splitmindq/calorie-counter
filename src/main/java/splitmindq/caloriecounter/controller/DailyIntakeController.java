package splitmindq.caloriecounter.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.DailyIntake;
import splitmindq.caloriecounter.service.DailyIntakeService;

@RestController
@RequestMapping("/api/v1/daily_intakes")
@AllArgsConstructor
public class DailyIntakeController {
    private DailyIntakeService dailyIntakeService;

    @GetMapping
    public ResponseEntity<List<DailyIntake>> getAll() {
        List<DailyIntake> dailyIntakeList = dailyIntakeService.getAllDailyIntakes();
        if (dailyIntakeList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(dailyIntakeList, HttpStatus.OK);
    }

    @PostMapping("create_intake")
    public ResponseEntity<String> createDailyIntake(
            @RequestParam Long userId,
            @RequestParam List<Long> foodIds) {
        try {
            dailyIntakeService.createDailyIntake(userId, foodIds);
            return ResponseEntity.status(HttpStatus.CREATED).body("Daily intake created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating daily intake: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DailyIntake> getDailyIntake(@PathVariable Long id) {
        DailyIntake dailyIntake = dailyIntakeService.getDailyIntakeById(id);
        if (dailyIntake == null) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(dailyIntake, HttpStatus.OK);
        }
    }

    @PutMapping("update_intake/{id}")
    public ResponseEntity<String> updateDailyIntake(
            @PathVariable Long id,
            @RequestBody DailyIntake updatedDailyIntake) {
        try {
            dailyIntakeService.updateDailyIntake(id, updatedDailyIntake);
            return ResponseEntity.status(HttpStatus.OK).body("Daily intake updated successfully.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Daily intake not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("delete_intake/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        try {
            dailyIntakeService.deleteDailyIntake(id);
            return ResponseEntity.status(HttpStatus.OK).body("Daily intake deleted successfully.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Daily intake not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }
}
