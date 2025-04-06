package splitmindq.caloriecounter.requests;

import java.util.List;
import lombok.Data;
import splitmindq.caloriecounter.model.FoodEntry;

@Data
public class DailyIntakeRequest {
    private Long userId;
    private List<FoodEntry> foodEntries;
}