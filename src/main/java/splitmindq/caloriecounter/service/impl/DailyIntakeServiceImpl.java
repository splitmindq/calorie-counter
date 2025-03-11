package splitmindq.caloriecounter.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.dao.DailyIntakeRepository;
import splitmindq.caloriecounter.model.DailyIntake;
import splitmindq.caloriecounter.service.DailyIntakeService;

@Primary
@Service
@AllArgsConstructor
public class DailyIntakeServiceImpl implements DailyIntakeService {
    private final DailyIntakeRepository dailyIntakeRepository;

    @Override
    public List<DailyIntake> getAllDailyIntakes() {
        return dailyIntakeRepository.findAll();
    }

    @Override
    public void createDailyIntake(DailyIntake dailyIntake) {
        dailyIntakeRepository.save(dailyIntake);
    }

    @Override
    public DailyIntake getDailyIntakeById(Long id) {
        return dailyIntakeRepository.findById(id).orElse(null);
    }

    @Override
    public void updateDailyIntake(DailyIntake dailyIntake) {
        dailyIntakeRepository.save(dailyIntake);
    }

    @Override
    public void deleteDailyIntake(Long id) {
        dailyIntakeRepository.deleteById(id);
    }
}
