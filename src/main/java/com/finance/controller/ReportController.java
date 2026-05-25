package com.finance.controller;

import com.finance.dto.ReportDTO;
import com.finance.service.AuthService;
import com.finance.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private AuthService authService;

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<?> getMonthlyReport(@PathVariable int year, @PathVariable int month, HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            ReportDTO.MonthlyReportResponse report = reportService.getMonthlyReport(userId, year, month);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<?> getYearlyReport(@PathVariable int year, HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            ReportDTO.YearlyReportResponse report = reportService.getYearlyReport(userId, year);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
