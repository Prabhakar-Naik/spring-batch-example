package com.learn.springbatchexample.banking;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.ZoneId;

/**
 * @author prabhakar, @Date 23-06-2025
 */

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final EntityManagerFactory entityManagerFactory;

    public BatchConfig(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    // Reader for CSV file – reads raw transaction data
    // Source
    @Bean
    public FlatFileItemReader<BankingTransaction> bankingTransactionItemReader() {
        return new FlatFileItemReaderBuilder<BankingTransaction>()
                .name("bankingTransactionItemReader")
                .resource(new ClassPathResource("transactions.csv"))
                .linesToSkip(1)  // This line tells Spring Batch to skip the header
                .delimited()
                .names(new String[]{"accountNumber", "amount", "transactionDate"})
                .fieldSetMapper(fieldSet -> {
                    BankingTransaction transaction = new BankingTransaction();
                    transaction.setAccountNumber(fieldSet.readString("accountNumber"));
                    transaction.setAmount(fieldSet.readBigDecimal("amount"));
                    // Convert the date string to LocalDateTime (assumes format yyyy-MM-dd)
                    transaction.setTransactionDate(
                            fieldSet.readDate("transactionDate", "yyyy-MM-dd")
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime());
                    // Set initial status to PENDING
                    transaction.setStatus("PENDING");
                    return transaction;
                })
                .build();
    }

    // Processor for Step 1: Validates the transaction. For instance, skip if the amount is negative.
    @Bean
    public ItemProcessor<BankingTransaction, BankingTransaction> bankingTransactionItemProcessor() {
        return transaction -> {
            if (transaction.getAmount().doubleValue() < 0) {
                // Returning null filters out the record so that invalid transactions are skipped.
                return null;
            }
            return transaction;
        };
    }

    // JPA Writer to persist transactions to the database
    @Bean
    public JpaItemWriter<BankingTransaction> jpaBankTransactionItemWriter() {
        return new JpaItemWriterBuilder<BankingTransaction>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    //
//    @Bean
//    public JpaItemWriter<BankingTransaction> jpaBankTransactionItemWriter(EntityManagerFactory entityManagerFactory) {
//        return new JpaItemWriterBuilder<BankingTransaction>()
//                .entityManagerFactory(entityManagerFactory)
//                .build();
//    }

    // Step 1: Import transactions from the CSV into the database using chunk processing
    @Bean
    public Step importTransactionStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("importTransactionStep", jobRepository)
                .<BankingTransaction, BankingTransaction>chunk(5, transactionManager)
                .reader(bankingTransactionItemReader())
                .processor(bankingTransactionItemProcessor())
                .writer(jpaBankTransactionItemWriter())
                .build();
    }

    // Reader for Step 2: Uses a JPA reader to pick up transactions that are still PENDING
    @Bean
    public JpaPagingItemReader<BankingTransaction> pendingTransactionReader() {
        return new JpaPagingItemReaderBuilder<BankingTransaction>()
                .name("pendingTransactionReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT t FROM BankingTransaction t WHERE t.status = 'PENDING'")
                .pageSize(5)
                .build();
    }

    // Processor for Step 2: Applies business logic—here, the transaction status is updated.
    @Bean
    public ItemProcessor<BankingTransaction, BankingTransaction> transactionStatusProcessor() {
        return transaction -> {
            // Simulate business processing: update the status to PROCESSED.
            transaction.setStatus("PROCESSED");
            return transaction;
        };
    }

    // Step 2: Processes pending transactions and updates their status in chunks.
    @Bean
    public Step processTransactionStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager) {
        return new StepBuilder("processTransactionStep", jobRepository)
                .<BankingTransaction, BankingTransaction>chunk(5, transactionManager)
                .reader(pendingTransactionReader())
                .processor(transactionStatusProcessor())
                .writer(jpaBankTransactionItemWriter())
                .build();
    }

    @Bean
    public Step noTransactionStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder("noTransactionStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("No pending transactions found.");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // Job that runs both steps sequentially
//    @Bean
//    public Job bankingTransactionJob(JobRepository jobRepository, Step importTransactionStep, Step processTransactionStep) {
//        return new JobBuilder("bankingTransactionJob", jobRepository)
//                .start(importTransactionStep)
//                .next(processTransactionStep)
//                .build();
//    }

    // Job: Builds the flow using a decider to determine if processing is needed.
    @Bean
    public Job bankingTransactionJob(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     Step importTransactionStep,
                                     Step processTransactionStep,
                                     Step noTransactionStep,
                                     TransactionDecider decider,
                                     JobCompletionNotificationListener listener) {
        return new JobBuilder("bankingTransactionJob", jobRepository)
                .listener(listener)
                .start(importTransactionStep)
                .next(decider)
                .on("PROCESS").to(processTransactionStep)
                .from(decider)
                .on("NO_PENDING").to(noTransactionStep)
                .end()
                .build();
    }



}
