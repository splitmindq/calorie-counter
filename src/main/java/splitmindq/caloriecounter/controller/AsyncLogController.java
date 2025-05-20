package splitmindq.caloriecounter.controller;

import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import splitmindq.caloriecounter.service.LogService;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/logs")
public class AsyncLogController {
    private final LogService logService;

    public AsyncLogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createLog(@RequestParam String date) {
        try {
            LocalDate logDate = LocalDate.parse(date);
            String taskId = logService.createLogForDate(logDate);
            return ResponseEntity.accepted()
                    .body(Map.of(
                            "taskId", taskId,
                            "statusUrl", "/api/logs/" + taskId + "/status",
                            "downloadUrl", "/api/logs/" + taskId + "/file"
                    ));
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Use YYYY-MM-DD");
        }
    }

    @GetMapping("/{taskId}/status")
    public ResponseEntity<Map<String, String>> getStatus(@PathVariable String taskId) {
        return ResponseEntity.ok(Map.of(
                "status", logService.getTaskStatus(taskId)
        ));
    }

    @GetMapping("/{taskId}/file")
    public ResponseEntity<Resource> getLogFile(@PathVariable String taskId) {
        try {
            File file = logService.getLogFile(taskId);
            Path path = Paths.get(file.getAbsolutePath());
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Log file not ready");
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File access error");
        }
    }
}