package lfh.project.financetracker.repository;

import lfh.project.financetracker.entity.Transaction;
import lfh.project.financetracker.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByAccountUserIdOrderByTimestampDesc(Long userId);

    List<Transaction> findByAccountUserIdAndTypeOrderByTimestampDesc(Long userId, TransactionType type);

    List<Transaction> findByAccountUserIdAndTimestampBetweenOrderByTimestampDesc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Transaction> findByAccountUserIdAndTypeAndTimestampBetweenOrderByTimestampDesc(
            Long userId,
            TransactionType type,
            LocalDateTime start,
            LocalDateTime end
    );
}