package splitmindq.caloriecounter.service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterService {
    private final Map<String, AtomicLong> urlCounters = new ConcurrentHashMap<>();

    public void incrementCounter(String url) {
        urlCounters.computeIfAbsent(url, k -> new AtomicLong()).incrementAndGet();
    }

    public long getVisitCount(String url) {
        String decodedUrl = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8);
        if (!decodedUrl.startsWith("/")) {
            decodedUrl = "/" + decodedUrl;
        }
        return urlCounters.computeIfAbsent(decodedUrl, k -> new AtomicLong()).get();
    }

    public Map<String, Long> getAllVisits() {
        Map<String, Long> result = new HashMap<>();
        urlCounters.forEach((url, counter) -> result.put(url, counter.get()));
        return Collections.unmodifiableMap(result);
    }
}