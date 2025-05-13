package splitmindq.caloriecounter.model;

import lombok.Data;

@Data
public class FoodEntry {
    private Long foodId;
    private double weight;

    public FoodEntry(Long foodId, double weight) {
        this.foodId = foodId;
        this.weight = weight;
    }
}
