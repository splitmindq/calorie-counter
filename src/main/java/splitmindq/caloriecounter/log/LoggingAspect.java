package splitmindq.caloriecounter.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Before("execution(* splitmindq.caloriecounter.controller.*.*(..))")
    public void logBeforeControllerMethod(JoinPoint joinPoint) {
        log.info("Вызов метода: {} с аргументами: {}",
                joinPoint.getSignature().toShortString(),
                joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "execution(* splitmindq.caloriecounter.controller.*.*(..))",
            returning = "result")
    public void logAfterControllerMethod(JoinPoint joinPoint, Object result) {
        log.info("Метод {} успешно выполнен. Результат: {}",
                joinPoint.getSignature().toShortString(),
                result);
    }

    @AfterThrowing(pointcut = "execution(* splitmindq.caloriecounter..*.*(..))",
            throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.error("Ошибка в методе: {}. Исключение: {} - {}",
                joinPoint.getSignature().toShortString(),
                ex.getClass().getName(),
                ex.getMessage());
    }
}