package splitmindq.caloriecounter.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import splitmindq.caloriecounter.dto.DailyNutritionDto;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.DailyIntake;
import splitmindq.caloriecounter.requests.AddFoodToDailyIntakeRequest;
import splitmindq.caloriecounter.requests.DailyIntakeRequest;
import splitmindq.caloriecounter.requests.UpdateDailyIntakeRequest;
import splitmindq.caloriecounter.service.DailyIntakeService;

@RestController
@RequestMapping("/api/v1/daily_intakes")
@AllArgsConstructor
@Validated
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

    @GetMapping("/filter")
    public List<DailyIntake> getIntakesByUserAndDate(
            @RequestParam String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dailyIntakeService.getUserIntakes(email, date);
    }

    @GetMapping("/nutrition")
    public ResponseEntity<?> getNutrition(
            @RequestParam @NotBlank @Email String email,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            DailyNutritionDto result = dailyIntakeService.getDailyNutrition(email, date);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
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
