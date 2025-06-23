package com.learn.springbatchexample.banking;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author prabhakar, @Date 23-06-2025
 */

@RestController
@RequestMapping("/api")
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Job bankingTransactionJob;

    public BatchJobController(JobLauncher jobLauncher, Job bankingTransactionJob) {
        this.jobLauncher = jobLauncher;
        this.bankingTransactionJob = bankingTransactionJob;
    }

    @GetMapping(value = "/import")
    public void loadDataToDB() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt",System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(bankingTransactionJob, jobParameters);
    }

    @PostMapping("/startBatchJob")
    public ResponseEntity<String> startBatchJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(bankingTransactionJob, jobParameters);
            // The listeners will be invoked automatically by Spring Batch.
            return ResponseEntity.ok("Job started successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error starting job: " + e.getMessage());
        }
    }

}

