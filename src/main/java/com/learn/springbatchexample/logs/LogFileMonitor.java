package com.learn.springbatchexample.logs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.PrintWriter;

/**
 * @author prabhakar, @Date 23-06-2025
 */
@Component
public class LogFileMonitor {

    // Define the file path and size limit (5MB).
    @Value("${log.csv.path}")
    private String logFilePath;

    private static final long SIZE_LIMIT = 5 * 1024 * 1024; // 5 MB in bytes

    private final JobLauncher jobLauncher;
    private final Job logAggregationJob;

    public LogFileMonitor(JobLauncher jobLauncher, Job logAggregationJob) {
        this.jobLauncher = jobLauncher;
        this.logAggregationJob = logAggregationJob;
    }

    // Check every 60 seconds (adjust as needed)
    @Scheduled(fixedDelay = 60000)
    public void checkFileSizeAndTriggerJob() {
        File logFile = new File(logFilePath);
        if (logFile.exists() && logFile.length() >= SIZE_LIMIT) {
            try {
                // Launch the log aggregation batch job.
                JobParameters jobParameters = new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters();

                jobLauncher.run(logAggregationJob, jobParameters);

                // Option 1: Clear the file content after processing.
                clearFileContent(logFile);

                // Option 2: Alternatively, archive the processed file:
                // archiveFile(logFile);

            } catch (Exception e) {
                // Handle exceptions or add logging for failures.
                e.printStackTrace();
            }
        }
    }

    private void clearFileContent(File file) throws Exception {
        // Overwrites the file with empty content to clear the logs.
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print("");
        }
    }

    // Optionally, you can implement a method to move the file to an archive directory.
    /*
    private void archiveFile(File file) throws Exception {
        File archiveDir = new File("archive");
        if (!archiveDir.exists()) {
            archiveDir.mkdirs();
        }
        String archiveFileName = file.getName() + "_" + System.currentTimeMillis();
        File archivedFile = new File(archiveDir, archiveFileName);
        Files.move(file.toPath(), archivedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        // After archiving, you might need to create a new empty log file.
        file.createNewFile();
    }
    */

}
