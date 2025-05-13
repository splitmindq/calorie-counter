package splitmindq.caloriecounter.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

// Итоговый ответ
@Data
@AllArgsConstructor
public class BatchFoodAddResponse {
    private List<FoodAddResult> results;
    private int successCount;
    private int failedCount;
}
