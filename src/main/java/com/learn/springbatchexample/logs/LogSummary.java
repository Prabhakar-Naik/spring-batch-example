package com.learn.springbatchexample.logs;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * @author prabhakar, @Date 23-06-2025
 */

@Entity
@Table(name = "log_summary")
public class LogSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String level;

    // Grouped by date (extracted from the log entry’s timestamp)
    private LocalDate logDate;

    private Integer count;

    // Getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getLevel() {
        return level;
    }
    public void setLevel(String level) {
        this.level = level;
    }
    public LocalDate getLogDate() {
        return logDate;
    }
    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }
    public Integer getCount() {
        return count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "LogSummary{" +
                "id=" + id +
                ", level='" + level + '\'' +
                ", logDate=" + logDate +
                ", count=" + count +
                '}';
    }
}
