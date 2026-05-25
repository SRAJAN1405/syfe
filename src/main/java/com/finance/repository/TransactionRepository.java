package com.finance.repository;

import com.finance.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByUserIdAndCategory(Long userId, String category);

    List<Transaction> findByUserIdAndType(Long userId, String type);

    List<Transaction> findByUserIdAndDateBetweenAndCategory(Long userId, LocalDate startDate, LocalDate endDate, String category);

    List<Transaction> findByUserIdAndDateBetweenAndType(Long userId, LocalDate startDate, LocalDate endDate, String type);

    List<Transaction> findByUserIdAndCategoryAndType(Long userId, String category, String type);

    List<Transaction> findByUserIdAndDateBetweenAndCategoryAndType(Long userId, LocalDate startDate, LocalDate endDate, String category, String type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND t.date >= :startDate")
    BigDecimal sumByUserIdAndTypeAndDateAfter(@Param("userId") Long userId, @Param("type") String type, @Param("startDate") LocalDate startDate);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND MONTH(t.date) = :month AND YEAR(t.date) = :year GROUP BY t.category")
    List<Object[]> getCategorySummary(@Param("userId") Long userId, @Param("type") String type, @Param("month") int month, @Param("year") int year);
}
