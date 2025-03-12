package splitmindq.caloriecounter.service;

import java.util.List;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.model.DailyIntake;

@Service
public interface DailyIntakeService {
    List<DailyIntake> getAllDailyIntakes();

    void createDailyIntake(Long userId, List<Long> foodIds);

    DailyIntake getDailyIntakeById(Long id);

    void updateDailyIntake(Long id, DailyIntake dailyIntake);

    void deleteDailyIntake(Long id);
}
