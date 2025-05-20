package splitmindq.caloriecounter.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
@RestController
@Tag(name = "Контроллер логирования", description = "API для управления логами")
@RequestMapping("/api/v1/logs")
public class LogController {

    private static final String MAIN_LOG_PATH = "./logs/application.log";
    private static final String ARCHIVE_DIR = "./logs/archive/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping(value = "/download-daily/{date}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadDailyLogFile(@PathVariable String date) {
        try {
            LocalDate filterDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            String datePrefix = filterDate.format(DATE_FORMATTER);
            String fileName = "application_" + date + ".log";
            Path filePath = Paths.get(ARCHIVE_DIR + fileName);

            if (Files.exists(filePath)) {
                return prepareFileResponse(filePath, fileName);
            }

            Files.createDirectories(Paths.get(ARCHIVE_DIR));

            List<String> filteredLines = Files.lines(Paths.get(MAIN_LOG_PATH))
                    .filter(line -> line.startsWith(datePrefix))
                    .collect(Collectors.toList());

            Files.write(filePath, filteredLines);

            return prepareFileResponse(filePath, fileName);

        } catch (DateTimeParseException e) {
            log.warn("Invalid date format received: {}", date);
            return ResponseEntity.badRequest().body(null);
        } catch (NoSuchFileException e) {
            log.warn("Main log file not found: {}", MAIN_LOG_PATH);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Error creating/downloading daily log file", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    private ResponseEntity<Resource> prepareFileResponse(Path filePath, String fileName) throws IOException {
        File file = filePath.toFile();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}