package splitmindq.caloriecounter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.List;
import lombok.Data;

@Data
@Entity
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Float calories;
    private Float protein;
    private Float fats;
    private Float carbs;

    @ManyToMany(mappedBy = "foods")
    private List<DailyIntake> dailyIntakes;
}