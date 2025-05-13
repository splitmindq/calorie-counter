package splitmindq.caloriecounter.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import splitmindq.caloriecounter.service.VisitCounterService;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private final VisitCounterService visitCounterService;

    public LoggingInterceptor(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String url = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = (queryString != null) ? url + "?" + queryString : url;

        // Нормализация URL: добавляем / в начало, если его нет
        if (!fullUrl.startsWith("/")) {
            fullUrl = "/" + fullUrl;
        }

        visitCounterService.incrementCounter(fullUrl);
        return true;
    }
}