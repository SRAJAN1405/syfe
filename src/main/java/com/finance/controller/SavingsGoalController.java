package com.finance.controller;

import com.finance.dto.SavingsGoalDTO;
import com.finance.service.AuthService;
import com.finance.service.SavingsGoalService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    @Autowired
    private SavingsGoalService savingsGoalService;

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<?> createGoal(@Valid @RequestBody SavingsGoalDTO.CreateRequest request, HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            SavingsGoalDTO.GoalResponse response = savingsGoalService.createGoal(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllGoals(HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            List<SavingsGoalDTO.GoalResponse> goals = savingsGoalService.getAllGoals(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("goals", goals);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGoal(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            SavingsGoalDTO.GoalResponse goal = savingsGoalService.getGoal(id, userId);
            return ResponseEntity.ok(goal);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            HttpStatus status = e.getMessage().equals("Goal not found") ?
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable Long id,
                                         @Valid @RequestBody SavingsGoalDTO.UpdateRequest request,
                                         HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            SavingsGoalDTO.GoalResponse response = savingsGoalService.updateGoal(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            HttpStatus status = e.getMessage().equals("Goal not found") ?
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            savingsGoalService.deleteGoal(id, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Goal deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            HttpStatus status = e.getMessage().equals("Goal not found") ?
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(error);
        }
    }
}
