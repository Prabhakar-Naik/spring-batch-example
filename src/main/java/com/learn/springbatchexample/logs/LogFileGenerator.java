package com.learn.springbatchexample.logs;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * @author prabhakar, @Date 23-06-2025
 */

@Component
public class LogFileGenerator {

    private static final Path LOG_FILE_PATH = Paths.get("src/main/resources/logs.csv");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Random random = new Random();

    // Sample values for log properties
    private final String[] levels = {"INFO", "ERROR", "WARN"};
    private final String[] components = {"Auth", "Payment", "Order", "Database", "Notification"};
    private final String[] messages = {
            "User logged in successfully.",
            "Payment processed successfully.",
            "Order delayed due to inventory check.",
            "Database connection lost.",
            "Invalid credentials provided."
    };

    // Constructor to initialize the CSV file; creates file with header if it doesn't exist.
    public LogFileGenerator() {
        try {
            if (!Files.exists(LOG_FILE_PATH)) {
                Files.write(LOG_FILE_PATH, "timestamp,level,component,message\n".getBytes(), StandardOpenOption.CREATE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // This method generates a log every 5 seconds.
    @Scheduled(fixedDelay = 5000)
    public void generateRandomLogEntry() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String level = levels[random.nextInt(levels.length)];
        String component = components[random.nextInt(components.length)];
        String message = messages[random.nextInt(messages.length)];

        String logEntry = String.format("%s,%s,%s,%s\n", timestamp, level, component, message);

        try {
            Files.write(LOG_FILE_PATH, logEntry.getBytes(), StandardOpenOption.APPEND);
            System.out.println("Generated log: " + logEntry.trim());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
