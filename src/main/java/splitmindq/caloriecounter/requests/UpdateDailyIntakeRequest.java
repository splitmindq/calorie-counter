package splitmindq.caloriecounter.requests;

import java.util.List;
import lombok.Data;

@Data
public class UpdateDailyIntakeRequest {
    private List<Long> foodIds; // Список ID продуктов
    private List<Double> weights; // Список весов
}