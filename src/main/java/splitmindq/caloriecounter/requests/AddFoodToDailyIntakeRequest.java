package splitmindq.caloriecounter.requests;

import lombok.Data;

@Data
public class AddFoodToDailyIntakeRequest {
    private Long foodId; // ID продукта
    private double weight; // Вес продукта в граммах
}