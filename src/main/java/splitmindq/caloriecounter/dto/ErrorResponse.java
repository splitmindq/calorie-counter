package splitmindq.caloriecounter.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private Map<String, List<String>> fieldErrors;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public ErrorResponse(Map<String, List<String>> fieldErrors) {
        this.fieldErrors = fieldErrors;
        this.message = "Validation failed";
    }
}