package com.finance.service;

import com.finance.dto.ReportDTO;
import com.finance.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private TransactionRepository transactionRepository;

    public ReportDTO.MonthlyReportResponse getMonthlyReport(Long userId, int year, int month) {
        List<Object[]> incomeData = transactionRepository.getCategorySummary(userId, "INCOME", month, year);
        List<Object[]> expenseData = transactionRepository.getCategorySummary(userId, "EXPENSE", month, year);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();

        BigDecimal totalIncomeAmount = BigDecimal.ZERO;
        BigDecimal totalExpenseAmount = BigDecimal.ZERO;

        for (Object[] data : incomeData) {
            String category = (String) data[0];
            BigDecimal amount = (BigDecimal) data[1];
            totalIncome.put(category, amount);
            totalIncomeAmount = totalIncomeAmount.add(amount);
        }

        for (Object[] data : expenseData) {
            String category = (String) data[0];
            BigDecimal amount = (BigDecimal) data[1];
            totalExpenses.put(category, amount);
            totalExpenseAmount = totalExpenseAmount.add(amount);
        }

        ReportDTO.MonthlyReportResponse response = new ReportDTO.MonthlyReportResponse();
        response.setMonth(month);
        response.setYear(year);
        response.setTotalIncome(totalIncome);
        response.setTotalExpenses(totalExpenses);
        response.setNetSavings(totalIncomeAmount.subtract(totalExpenseAmount));

        return response;
    }

    public ReportDTO.YearlyReportResponse getYearlyReport(Long userId, int year) {
        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();

        BigDecimal totalIncomeAmount = BigDecimal.ZERO;
        BigDecimal totalExpenseAmount = BigDecimal.ZERO;

        for (int month = 1; month <= 12; month++) {
            List<Object[]> incomeData = transactionRepository.getCategorySummary(userId, "INCOME", month, year);
            List<Object[]> expenseData = transactionRepository.getCategorySummary(userId, "EXPENSE", month, year);

            for (Object[] data : incomeData) {
                String category = (String) data[0];
                BigDecimal amount = (BigDecimal) data[1];
                totalIncome.merge(category, amount, BigDecimal::add);
                totalIncomeAmount = totalIncomeAmount.add(amount);
            }

            for (Object[] data : expenseData) {
                String category = (String) data[0];
                BigDecimal amount = (BigDecimal) data[1];
                totalExpenses.merge(category, amount, BigDecimal::add);
                totalExpenseAmount = totalExpenseAmount.add(amount);
            }
        }

        ReportDTO.YearlyReportResponse response = new ReportDTO.YearlyReportResponse();
        response.setYear(year);
        response.setTotalIncome(totalIncome);
        response.setTotalExpenses(totalExpenses);
        response.setNetSavings(totalIncomeAmount.subtract(totalExpenseAmount));

        return response;
    }
}
