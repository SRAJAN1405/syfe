package com.finance.controller;

import com.finance.dto.TransactionDTO;
import com.finance.service.AuthService;
import com.finance.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionDTO.CreateRequest request, HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            TransactionDTO.TransactionResponse response = transactionService.createTransaction(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            List<TransactionDTO.TransactionResponse> transactions =
                transactionService.getTransactions(userId, startDate, endDate, category, type);
            Map<String, Object> response = new HashMap<>();
            response.put("transactions", transactions);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id,
                                                @Valid @RequestBody TransactionDTO.UpdateRequest request,
                                                HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            TransactionDTO.TransactionResponse response = transactionService.updateTransaction(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            HttpStatus status = e.getMessage().equals("Transaction not found") ?
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = authService.getCurrentUserId(session);
            transactionService.deleteTransaction(id, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Transaction deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            HttpStatus status = e.getMessage().equals("Transaction not found") ?
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(error);
        }
    }
}
