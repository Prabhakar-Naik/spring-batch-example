package com.learn.springbatchexample.banking;

import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

/**
 * @author prabhakar, @Date 23-06-2025
 */
@Component
public class TransactionDecider implements JobExecutionDecider {

    private final BankingTransactionRepository repository;

    public TransactionDecider(BankingTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public @NotNull FlowExecutionStatus decide(@NotNull JobExecution jobExecution, StepExecution stepExecution) {
        long pendingCount = repository.countByStatus("PENDING");
        if (pendingCount > 0) {
            return new FlowExecutionStatus("PROCESS");
        } else {
            return new FlowExecutionStatus("NO_PENDING");
        }
    }

}
