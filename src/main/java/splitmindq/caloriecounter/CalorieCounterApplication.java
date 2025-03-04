package splitmindq.caloriecounter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения CalorieCounter.
 * Используется для запуска Spring Boot приложения.
 */
@SpringBootApplication
public class CalorieCounterApplication {
    public static void main(String[] args) {
        SpringApplication.run(CalorieCounterApplication.class, args);
    }
}
