package splitmindq.caloriecounter.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;
@Data
@Entity
@ToString
@Table(name = "daily_intake_food")
public class DailyIntakeFood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "daily_intake_id", nullable = false)
    @ToString.Exclude
    private DailyIntake dailyIntake;

    @ManyToOne
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    private double weight;
}
