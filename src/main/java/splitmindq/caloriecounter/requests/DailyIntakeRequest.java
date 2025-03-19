package splitmindq.caloriecounter.requests;

import lombok.Data;
import splitmindq.caloriecounter.model.FoodEntry;

import java.util.List;

@Data
public class DailyIntakeRequest {
    private Long userId;
    private List<FoodEntry> foodEntries;
}