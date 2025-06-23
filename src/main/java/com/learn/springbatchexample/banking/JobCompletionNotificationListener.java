package com.learn.springbatchexample.banking;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author prabhakar, @Date 23-06-2025
 */

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void beforeJob(@NotNull JobExecution jobExecution) {
        // Log that the job is starting with job ID and current timestamp.
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Job is starting. JobExecution ID: {} at {}", jobExecution.getId(), startTime);

        // Optionally, store the start time in the job execution context for later use.
        jobExecution.getExecutionContext().putLong("jobStartTime", System.currentTimeMillis());
    }

    @Override
    public void afterJob(@NotNull JobExecution jobExecution) {
        if (!jobExecution.getStatus().isRunning() && !jobExecution.getStatus().isUnsuccessful()) {
            log.info("Job completed successfully! Verifying the results...");
            entityManager.createQuery("SELECT t FROM BankingTransaction t", BankingTransaction.class)
                    .getResultList()
                    .forEach(t -> log.info("Found transaction: {}", t));
        } else {
            log.info("Job failed with status: {}", jobExecution.getStatus());
        }
    }
}