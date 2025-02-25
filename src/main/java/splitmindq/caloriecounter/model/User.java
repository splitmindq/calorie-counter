package splitmindq.caloriecounter.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String firstName;
    private String lastName;
    private int age;
    private String gender;
    private String email;
    private int weight;
    private int height;
}
