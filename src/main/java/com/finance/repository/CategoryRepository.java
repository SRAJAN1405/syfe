package com.finance.repository;

import com.finance.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsDefaultTrue();

    List<Category> findByUserId(Long userId);

    @Query("SELECT c FROM Category c WHERE c.isDefault = true OR c.userId = :userId")
    List<Category> findAllByUserIdOrDefault(@Param("userId") Long userId);

    Optional<Category> findByNameAndUserId(String name, Long userId);

    boolean existsByNameAndIsDefaultTrue(String name);

    boolean existsByNameAndUserId(String name, Long userId);
}