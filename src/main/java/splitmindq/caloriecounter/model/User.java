package splitmindq.caloriecounter.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Data;
/**
 * Модель пользователя, содержащая основную информацию о пользователе.
 * Используется для сохранения и передачи данных о пользователе.
 */

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя.
     */
    private String firstName;

    /**
     * Фамилия пользователя.
     */
    private String lastName;

    /**
     * Возраст пользователя.
     */
    private int age;

    /**
     * Пол пользователя.
     */
    private String gender;

    /**
     * Адрес электронной почты пользователя.
     */
    @Column(unique = true)
    private String email;

    /**
     * Вес пользователя (в килограммах).
     */
    private int weight;

    /**
     * Рост пользователя (в сантиметрах).
     */
    private int height;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DailyIntake> dailyIntakes;
}
