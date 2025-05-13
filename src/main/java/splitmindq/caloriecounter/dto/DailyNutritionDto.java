package splitmindq.caloriecounter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyNutritionDto {
    private double calories;
    private double protein;
    private double fats;
    private double carbs;

    public DailyNutritionDto(Map<String, Double> data) {
        this.calories = data.getOrDefault("calories", 0.0);
        this.protein = data.getOrDefault("protein", 0.0);
        this.fats = data.getOrDefault("fats", 0.0);
        this.carbs = data.getOrDefault("carbs", 0.0);
    }


}