package splitmindq.caloriecounter.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FoodAddResult {
    private Long foodId;
    private boolean success;
    private String message;
}
