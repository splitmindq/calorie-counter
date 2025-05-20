package splitmindq.caloriecounter.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import splitmindq.caloriecounter.service.VisitCounterService;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/visits")
public class VisitController {
    private final VisitCounterService visitCounterService;

    public VisitController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @PostMapping("/track")
    public ResponseEntity<String> trackVisit(@RequestParam String url) {
        visitCounterService.incrementCounter(url);
        return ResponseEntity.ok("Counter incremented for URL: " + url);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getVisitCount(@RequestParam String url) {
        return ResponseEntity.ok(visitCounterService.getVisitCount(url));
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Long>> getAllVisits() {
        return ResponseEntity.ok(visitCounterService.getAllVisits());
    }
}