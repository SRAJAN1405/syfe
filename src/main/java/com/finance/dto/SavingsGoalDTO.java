package com.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsGoalDTO {

    public static class CreateRequest {
        @NotBlank(message = "Goal name is required")
        private String goalName;

        @NotNull(message = "Target amount is required")
        @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
        private BigDecimal targetAmount;

        @NotNull(message = "Target date is required")
        private LocalDate targetDate;

        private LocalDate startDate;

        public String getGoalName() { return goalName; }
        public void setGoalName(String goalName) { this.goalName = goalName; }

        public BigDecimal getTargetAmount() { return targetAmount; }
        public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

        public LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    }

    public static class UpdateRequest {
        @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
        private BigDecimal targetAmount;

        private LocalDate targetDate;

        public BigDecimal getTargetAmount() { return targetAmount; }
        public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

        public LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    }

    public static class GoalResponse {
        private Long id;
        private String goalName;
        private BigDecimal targetAmount;
        private LocalDate targetDate;
        private LocalDate startDate;
        private BigDecimal currentProgress;
        private BigDecimal progressPercentage;
        private BigDecimal remainingAmount;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getGoalName() { return goalName; }
        public void setGoalName(String goalName) { this.goalName = goalName; }

        public BigDecimal getTargetAmount() { return targetAmount; }
        public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

        public LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public BigDecimal getCurrentProgress() { return currentProgress; }
        public void setCurrentProgress(BigDecimal currentProgress) { this.currentProgress = currentProgress; }

        public BigDecimal getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(BigDecimal progressPercentage) { this.progressPercentage = progressPercentage; }

        public BigDecimal getRemainingAmount() { return remainingAmount; }
        public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }
    }
}
