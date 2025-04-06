package splitmindq.caloriecounter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Главный класс приложения CalorieCounter.
 * Используется для запуска Spring Boot приложения.
 */
@SpringBootApplication
@EnableCaching
public class CalorieCounterApplication {
    public static void main(String[] args) {
        SpringApplication.run(CalorieCounterApplication.class, args);
    }
}
