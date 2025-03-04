package splitmindq.caloriecounter.model;

import lombok.Builder;
import lombok.Data;

/**
 * Модель пользователя, содержащая основную информацию о пользователе.
 * Используется для сохранения и передачи данных о пользователе.
 */
@Data
@Builder
public class User {
    private Integer id;

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
    private String email;

    /**
     * Вес пользователя (в килограммах).
     */
    private int weight;

    /**
     * Рост пользователя (в сантиметрах).
     */
    private int height;
}
