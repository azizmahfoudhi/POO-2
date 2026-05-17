package com.ihec.club.repository;

import com.ihec.club.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour les budgets.
 */
public interface BudgetRepository extends JpaRepository<Budget, Long> {
}
