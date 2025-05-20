package splitmindq.caloriecounter.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.excpetions.NotFoundException;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

@Service
public class LogService {
    private static final String LOGS_DIR = "./logs/";
    private static final String ARCHIVE_DIR = LOGS_DIR + "archive/";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<String, LogTask> tasks = new ConcurrentHashMap<>();

    public String createLogForDate(LocalDate date) {
        String taskId = UUID.randomUUID().toString();
        LogTask task = new LogTask(taskId, date);
        tasks.put(taskId, task);
        executor.submit(task);
        return taskId;
    }

    public String getTaskStatus(String taskId) {
        LogTask task = tasks.get(taskId);
        return task != null ? task.getStatus() : "NOT_FOUND";
    }

    public File getLogFile(String taskId) throws FileNotFoundException {
        LogTask task = tasks.get(taskId);
        if (task == null || !"COMPLETED".equals(task.getStatus())) {
            throw new NotFoundException("Log file not ready");
        }
        return task.getLogFile();
    }

    private static class LogTask implements Runnable {
        private final String taskId;
        private final LocalDate date;
        @Getter
        private volatile String status = "PENDING";
        @Getter
        private File logFile;

        public LogTask(String taskId, LocalDate date) {
            this.taskId = taskId;
            this.date = date;
        }

        @Override
        public void run() {
            status = "RUNNING";
            try {
                Thread.sleep(10000);
                Files.createDirectories(Paths.get(ARCHIVE_DIR));
                String filename = "application_" + date.format(DATE_FORMAT) + ".log";
                logFile = new File(ARCHIVE_DIR + filename);
                filterAndSaveLogs(date, logFile);
                status = "COMPLETED";
            } catch (InterruptedException e) {
                // Восстанавливаем статус прерывания
                Thread.currentThread().interrupt();
                status = "INTERRUPTED";
            } catch (IOException e) {
                status = "FAILED";
            }
        }

        private void filterAndSaveLogs(LocalDate date, File outputFile) throws IOException {
            String datePrefix = date.format(DATE_FORMAT);
            List<String> filteredLines = Files.lines(Paths.get(LOGS_DIR + "application.log"))
                    .filter(line -> line.startsWith(datePrefix))
                    .toList();

            Files.write(outputFile.toPath(), filteredLines);
        }
    }
}