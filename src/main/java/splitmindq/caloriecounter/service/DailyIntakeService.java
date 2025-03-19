package splitmindq.caloriecounter.service;

import java.util.List;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.model.DailyIntake;
import splitmindq.caloriecounter.requests.DailyIntakeRequest;
import splitmindq.caloriecounter.requests.UpdateDailyIntakeRequest;

@Service
public interface DailyIntakeService {
    List<DailyIntake> getAllDailyIntakes();

    void createDailyIntake(DailyIntakeRequest dailyIntakeRequest);

    DailyIntake getDailyIntakeById(Long id);

    void updateDailyIntake(Long id, UpdateDailyIntakeRequest updateDailyIntake);

    DailyIntake addFoodToDailyIntake(Long dailyIntakeId, Long foodId, double weight);

    boolean deleteDailyIntake(Long id);
}