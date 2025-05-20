package splitmindq.caloriecounter.service;

import java.time.LocalDate;
import java.util.List;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.dto.DailyNutritionDto;
import splitmindq.caloriecounter.model.DailyIntake;
import splitmindq.caloriecounter.requests.DailyIntakeRequest;
import splitmindq.caloriecounter.requests.UpdateDailyIntakeRequest;

@Service
public interface DailyIntakeService {
    List<DailyIntake> getAllDailyIntakes();

    DailyNutritionDto getNutritionForIntake(Long intakeId);

    DailyIntake createDailyIntake(DailyIntakeRequest dailyIntakeRequest);

    DailyIntake getDailyIntakeById(Long id);

    void updateDailyIntake(Long id, UpdateDailyIntakeRequest updateDailyIntake);

    DailyIntake addFoodToDailyIntake(Long dailyIntakeId, Long foodId, double weight);

    boolean deleteDailyIntake(Long id);

    public List<DailyIntake> getUserIntakes(String email, @Nullable LocalDate date);

    public DailyNutritionDto getDailyNutrition(String email, LocalDate date);
}