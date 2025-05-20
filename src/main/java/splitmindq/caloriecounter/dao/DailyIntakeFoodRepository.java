package splitmindq.caloriecounter.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import splitmindq.caloriecounter.model.DailyIntakeFood;
import java.util.List;

public interface DailyIntakeFoodRepository extends JpaRepository<DailyIntakeFood, Long> {
    List<DailyIntakeFood> findByFoodId(Long foodId);

    List<DailyIntakeFood> findByDailyIntakeId(Long id);
}