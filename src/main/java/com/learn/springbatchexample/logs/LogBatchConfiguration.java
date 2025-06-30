package com.learn.springbatchexample.logs;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.format.DateTimeFormatter;

/**
 * @author prabhakar, @Date 23-06-2025
 */

@Configuration
public class LogBatchConfiguration {

    // Reader: Reads log entries from a CSV file.
    @Bean
    public FlatFileItemReader<LogEntry> logEntryItemReader() {
        return new FlatFileItemReaderBuilder<LogEntry>()
                .name("logEntryItemReader")
                .resource(new ClassPathResource("logs.csv"))
                .linesToSkip(1) // Skip header row.
                .delimited()
                .names("timestamp", "level", "component", "message")
                .fieldSetMapper(fieldSet -> {
                    LogEntry entry = new LogEntry();
                    // Parse the timestamp (format example: "yyyy-MM-dd HH:mm:ss")
                    String timestampStr = fieldSet.readString("timestamp");
                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    entry.setTimestamp(java.time.LocalDateTime.parse(timestampStr, formatter));
                    entry.setLevel(fieldSet.readString("level"));
                    entry.setComponent(fieldSet.readString("component"));
                    entry.setMessage(fieldSet.readString("message"));
                    return entry;
                })
                .build();
    }

    // Writer: Writes LogEntry items to the DB.
    @Bean
    public JpaItemWriter<LogEntry> logEntryItemWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<LogEntry>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    // Step 1: Import log entries from CSV into the database.
    @Bean
    public Step importLogEntriesStep(org.springframework.batch.core.repository.JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     EntityManagerFactory entityManagerFactory) {
        return new StepBuilder("importLogEntriesStep", jobRepository)
                .<LogEntry, LogEntry>chunk(5, transactionManager)
                .reader(logEntryItemReader())
                .writer(logEntryItemWriter(entityManagerFactory))
                .build();
    }

    // Step 2: Aggregate log data using a custom Tasklet.
    @Bean
    public Step aggregateLogDataStep(org.springframework.batch.core.repository.JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     LogAggregationTasklet logAggregationTasklet) {
        return new StepBuilder("aggregateLogDataStep", jobRepository)
                .tasklet(logAggregationTasklet, transactionManager)
                .build();
    }

    // Job: Combine both steps into a single job.
    @Bean
    public Job logAggregationJob(org.springframework.batch.core.repository.JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 Step importLogEntriesStep,
                                 Step aggregateLogDataStep) {
        return new JobBuilder("logAggregationJob", jobRepository)
                .start(importLogEntriesStep)
                .next(aggregateLogDataStep)
                .build();
    }
}
