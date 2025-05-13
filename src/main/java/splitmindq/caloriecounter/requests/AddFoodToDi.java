package splitmindq.caloriecounter.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class AddFoodToDi {
    @NotNull
    private Long dailyIntakeId;

    @NotNull
    private Long foodId;

    @Positive
    private double weight;
}

