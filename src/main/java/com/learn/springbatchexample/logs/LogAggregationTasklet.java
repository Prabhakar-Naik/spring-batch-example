package com.learn.springbatchexample.logs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * @author prabhakar, @Date 23-06-2025
 */
@Component
public class LogAggregationTasklet implements Tasklet {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) throws Exception {
        // Native SQL groups log entries by level and by date.
        String sql = "SELECT level, CAST(timestamp AS DATE) as log_date, COUNT(*) as cnt " +
                "FROM log_entry GROUP BY level, CAST(timestamp AS DATE)";

        @SuppressWarnings("unchecked")
        java.util.List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();

        results.forEach(row -> {
            String level = (String) row[0];
            // row[1] is a java.sql.Date which we convert to LocalDate.
            LocalDate logDate = ((java.sql.Date) row[1]).toLocalDate();
            Number count = (Number) row[2];

            LogSummary summary = new LogSummary();
            summary.setLevel(level);
            summary.setLogDate(logDate);
            summary.setCount(count.intValue());

            // Persist the aggregated summary record.
            entityManager.persist(summary);
        });

        return RepeatStatus.FINISHED;
    }
}
