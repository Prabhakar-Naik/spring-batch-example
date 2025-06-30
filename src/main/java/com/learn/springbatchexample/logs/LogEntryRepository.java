package com.learn.springbatchexample.logs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author prabhakar, @Date 23-06-2025
 */
@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
}
