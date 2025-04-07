package splitmindq.caloriecounter.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import splitmindq.caloriecounter.model.DailyIntake;
import splitmindq.caloriecounter.model.User;

public interface DailyIntakeRepository extends JpaRepository<DailyIntake, Long> {
    // jpql date not null
    @Query("""
            SELECT DISTINCT di FROM DailyIntake di
            JOIN FETCH di.user u
            LEFT JOIN FETCH di.dailyIntakeFoods dif
            LEFT JOIN FETCH dif.food f
            WHERE u.email = :email
            AND di.creationDate = :date
            ORDER BY di.creationDate DESC
            """)
    List<DailyIntake> findUserIntakesWithDate(
            @Param("email") String email,
            @Param("date") LocalDate date
    );

    // Запрос date null
    @Query("""
            SELECT DISTINCT di FROM DailyIntake di
            JOIN FETCH di.user u
            LEFT JOIN FETCH di.dailyIntakeFoods dif
            LEFT JOIN FETCH dif.food f
            WHERE u.email = :email
            ORDER BY di.creationDate DESC
            """)
    List<DailyIntake> findUserIntakesWithoutDate(@Param("email") String email);

    // native
    @Query(value = """
            SELECT 
                COALESCE(ROUND(SUM(f.calories * dif.weight / 100)::numeric, 1), 0.0) AS calories,
                COALESCE(ROUND(SUM(f.protein * dif.weight / 100)::numeric, 1), 0.0) AS protein,
                COALESCE(ROUND(SUM(f.fats * dif.weight / 100)::numeric, 1), 0.0) AS fats,
                COALESCE(ROUND(SUM(f.carbs * dif.weight / 100)::numeric, 1), 0.0) AS carbs
            FROM daily_intakes di
            JOIN users u ON di.user_id = u.id
            JOIN daily_intake_food dif ON di.id = dif.daily_intake_id
            JOIN foods f ON dif.food_id = f.id
            WHERE u.email = :email
            AND di.created_at = :date
            GROUP BY di.created_at
            """, nativeQuery = true)
    Optional<Map<String, Double>> calculateDailyNutrition(
            @Param("email") String email,
            @Param("date") LocalDate date
    );

    @Transactional
    @Modifying
    @Query("DELETE FROM DailyIntakeFood dif WHERE dif.dailyIntake.user.id = :userId")
    void deleteAllDailyIntakeFoodByUserId(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM DailyIntake di WHERE di.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}


