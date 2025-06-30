package com.learn.springbatchexample.banking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author prabhakar, @Date 23-06-2025
 */
@Repository
public interface BankingTransactionRepository extends JpaRepository<BankingTransaction, Long> {

    long countByStatus(String status);
}
