package lfh.project.financetracker.repository;

import lfh.project.financetracker.entity.Transaction;
import lfh.project.financetracker.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
    SELECT t FROM Transaction t
    WHERE t.account.id = :accountId
      AND (cast(:type as string) IS NULL OR t.type = :type)
      AND (cast(:startDate as timestamp) IS NULL OR t.timestamp >= :startDate)
      AND (cast(:endDate as timestamp) IS NULL OR t.timestamp <= :endDate)
    ORDER BY t.timestamp DESC
    """)
    List<Transaction> findByAccountIdWithFilters(
            @Param("accountId") Long accountId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
    SELECT t FROM Transaction t
    WHERE t.account.user.id = :userId
      AND (cast(:type as string) IS NULL OR t.type = :type)
      AND (cast(:startDate as timestamp) IS NULL OR t.timestamp >= :startDate)
      AND (cast(:endDate as timestamp) IS NULL OR t.timestamp <= :endDate)
    ORDER BY t.timestamp DESC
    """)
    List<Transaction> findByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}