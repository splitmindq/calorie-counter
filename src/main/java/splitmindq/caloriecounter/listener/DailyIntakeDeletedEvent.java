package splitmindq.caloriecounter.listener;

import org.springframework.context.ApplicationEvent;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class DailyIntakeDeletedEvent extends ApplicationEvent {
    private final String email;
    private final LocalDate date;
    private final Long intakeId;

    public DailyIntakeDeletedEvent(Object source, String email, LocalDate date, Long intakeId) {
        super(source);
        this.email = email;
        this.date = date;
        this.intakeId = intakeId;
    }
}