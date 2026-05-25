package com.finance.service;

import com.finance.dto.SavingsGoalDTO;
import com.finance.entity.SavingsGoal;
import com.finance.repository.SavingsGoalRepository;
import com.finance.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavingsGoalService {

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public SavingsGoalDTO.GoalResponse createGoal(SavingsGoalDTO.CreateRequest request, Long userId) {
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        SavingsGoal goal = new SavingsGoal(
            request.getGoalName(),
            request.getTargetAmount(),
            request.getTargetDate(),
            startDate,
            userId
        );

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return calculateProgress(saved);
    }

    public List<SavingsGoalDTO.GoalResponse> getAllGoals(Long userId) {
        return savingsGoalRepository.findByUserId(userId)
            .stream()
            .map(this::calculateProgress)
            .collect(Collectors.toList());
    }

    public SavingsGoalDTO.GoalResponse getGoal(Long id, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (!goal.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        return calculateProgress(goal);
    }

    @Transactional
    public SavingsGoalDTO.GoalResponse updateGoal(Long id, SavingsGoalDTO.UpdateRequest request, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (!goal.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        if (request.getTargetAmount() != null) goal.setTargetAmount(request.getTargetAmount());
        if (request.getTargetDate() != null) goal.setTargetDate(request.getTargetDate());

        SavingsGoal updated = savingsGoalRepository.save(goal);
        return calculateProgress(updated);
    }

    @Transactional
    public void deleteGoal(Long id, Long userId) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (!goal.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        savingsGoalRepository.delete(goal);
    }

    private SavingsGoalDTO.GoalResponse calculateProgress(SavingsGoal goal) {
        BigDecimal totalIncome = transactionRepository.sumByUserIdAndTypeAndDateAfter(
            goal.getUserId(), "INCOME", goal.getStartDate());
        BigDecimal totalExpenses = transactionRepository.sumByUserIdAndTypeAndDateAfter(
            goal.getUserId(), "EXPENSE", goal.getStartDate());

        BigDecimal currentProgress = totalIncome.subtract(totalExpenses);
        if (currentProgress.compareTo(BigDecimal.ZERO) < 0) {
            currentProgress = BigDecimal.ZERO;
        }

        BigDecimal progressPercentage = currentProgress
            .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        BigDecimal remainingAmount = goal.getTargetAmount().subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        SavingsGoalDTO.GoalResponse response = new SavingsGoalDTO.GoalResponse();
        response.setId(goal.getId());
        response.setGoalName(goal.getGoalName());
        response.setTargetAmount(goal.getTargetAmount());
        response.setTargetDate(goal.getTargetDate());
        response.setStartDate(goal.getStartDate());
        response.setCurrentProgress(currentProgress);
        response.setProgressPercentage(progressPercentage);
        response.setRemainingAmount(remainingAmount);

        return response;
    }
}
