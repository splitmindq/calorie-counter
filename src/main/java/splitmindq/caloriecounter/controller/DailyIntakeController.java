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
import splitmindq.caloriecounter.model.DailyIntake;
import splitmindq.caloriecounter.service.DailyIntakeService;

@RestController
@RequestMapping("/api/v1/daily_intakes")
@AllArgsConstructor
public class DailyIntakeController {
    private DailyIntakeService dailyIntakeService;

    @GetMapping
    public List<DailyIntake> getAll() {
        return dailyIntakeService.getAllDailyIntakes();
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
    public DailyIntake getDailyIntake(@PathVariable Long id) {
        return dailyIntakeService.getDailyIntakeById(id);
    }
//
//    @PutMapping("/update_intake")
//    public ResponseEntity<Void> updateDailyIntake(
//            @RequestParam Long id,
//            @RequestParam Long userId,
//            @RequestParam List<Long> foodIds) {
//        System.out.println("Метод updateDailyIntake вызван с id: " + id + ", userId: " + userId +
//                ", foodIds: " + foodIds);
//        return ResponseEntity.ok().build();
//    }

    @DeleteMapping("delete_intake/{id}")
    public void deleteDailyIntake(@PathVariable Long id) {
        dailyIntakeService.deleteDailyIntake(id);
    }
}
