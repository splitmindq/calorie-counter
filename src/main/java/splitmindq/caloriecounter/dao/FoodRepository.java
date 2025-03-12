package splitmindq.caloriecounter.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import splitmindq.caloriecounter.model.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {
    boolean existsByName(String name);
}
