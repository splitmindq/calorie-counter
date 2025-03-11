package splitmindq.caloriecounter.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import splitmindq.caloriecounter.model.DailyIntake;

public interface DailyIntakeRepository extends JpaRepository<DailyIntake, Long> {
}
