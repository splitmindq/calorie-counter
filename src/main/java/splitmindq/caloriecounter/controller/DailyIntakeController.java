package splitmindq.caloriecounter.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.DailyIntake;
import splitmindq.caloriecounter.requests.AddFoodToDailyIntakeRequest;
import splitmindq.caloriecounter.requests.DailyIntakeRequest;
import splitmindq.caloriecounter.requests.UpdateDailyIntakeRequest;
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
    public ResponseEntity<String> createDailyIntake(@RequestBody DailyIntakeRequest request) {
        try {
            dailyIntakeService.createDailyIntake(request);
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

    @PostMapping("/{dailyIntakeId}/add-food")
    public ResponseEntity<DailyIntake> addFoodToDailyIntake(
            @PathVariable Long dailyIntakeId,
            @RequestBody AddFoodToDailyIntakeRequest request) {
        DailyIntake dailyIntake = dailyIntakeService.addFoodToDailyIntake(
                dailyIntakeId,
                request.getFoodId(),
                request.getWeight()
        );
        return ResponseEntity.ok(dailyIntake);
    }

    @PatchMapping("/update_intake/{id}")
    public ResponseEntity<String> updateDailyIntake(
            @PathVariable Long id,
            @RequestBody UpdateDailyIntakeRequest request) {
        try {
            dailyIntakeService.updateDailyIntake(id, request);
            return ResponseEntity.status(HttpStatus.OK).body("Daily intake updated successfully.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Daily intake not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("delete_intake/{id}")
    public ResponseEntity<String> deleteDailyIntake(@PathVariable Long id) {
        boolean isDeleted = dailyIntakeService.deleteDailyIntake(id);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("DailyIntake not found.");
        }
    }
}
