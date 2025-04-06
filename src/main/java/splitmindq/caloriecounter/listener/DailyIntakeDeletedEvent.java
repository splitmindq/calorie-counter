package splitmindq.caloriecounter.listener;

import org.springframework.context.ApplicationEvent;
import splitmindq.caloriecounter.model.DailyIntake;
import java.time.LocalDate;

public class DailyIntakeDeletedEvent extends ApplicationEvent {
    private final String email;
    private final LocalDate date;

    public DailyIntakeDeletedEvent(DailyIntake source, String email, LocalDate date) {
        super(source);
        this.email = email;
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDate() {
        return date;
    }
}