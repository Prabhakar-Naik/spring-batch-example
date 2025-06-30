package com.learn.springbatchexample.logs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author prabhakar, @Date 23-06-2025
 */
@RestController
@RequestMapping("/logs")
public class LogController {

    @Autowired
    private LogEntryRepository logEntryRepository;

    @Autowired
    private LogSummaryRepository logSummaryRepository;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job logAggregationJob;


    @GetMapping
    public List<LogEntry> getAllLogs() {
        return logEntryRepository.findAll();
    }

    @GetMapping("/summary")
    public List<LogSummary> getLogSummary() {
        return logSummaryRepository.findAll();
    }

    @PostMapping("/trigger")
    public String triggerLogAggregationJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(logAggregationJob, jobParameters);
            return "Log aggregation job triggered successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error triggering log aggregation job: " + e.getMessage();
        }
    }
}
