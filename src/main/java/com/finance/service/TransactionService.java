package com.finance.service;

import com.finance.dto.TransactionDTO;
import com.finance.entity.Transaction;
import com.finance.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryService categoryService;

    @Transactional
    public TransactionDTO.TransactionResponse createTransaction(TransactionDTO.CreateRequest request, Long userId) {
        String type = request.getType();
        if (type == null || type.isBlank()) {
            type = categoryService.getCategoryType(request.getCategory(), userId);
        }

        Transaction transaction = new Transaction(
            request.getAmount(),
            request.getDate(),
            request.getCategory(),
            type,
            request.getDescription(),
            userId
        );

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public List<TransactionDTO.TransactionResponse> getTransactions(
            Long userId, LocalDate startDate, LocalDate endDate, String category, String type) {

        List<Transaction> transactions;

        if (startDate != null && endDate != null && category != null && type != null) {
            transactions = transactionRepository.findByUserIdAndDateBetweenAndCategoryAndType(userId, startDate, endDate, category, type);
        } else if (startDate != null && endDate != null && category != null) {
            transactions = transactionRepository.findByUserIdAndDateBetweenAndCategory(userId, startDate, endDate, category);
        } else if (startDate != null && endDate != null && type != null) {
            transactions = transactionRepository.findByUserIdAndDateBetweenAndType(userId, startDate, endDate, type);
        } else if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        } else if (category != null && type != null) {
            transactions = transactionRepository.findByUserIdAndCategoryAndType(userId, category, type);
        } else if (category != null) {
            transactions = transactionRepository.findByUserIdAndCategory(userId, category);
        } else if (type != null) {
            transactions = transactionRepository.findByUserIdAndType(userId, type);
        } else {
            transactions = transactionRepository.findByUserId(userId);
        }

        return transactions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public TransactionDTO.TransactionResponse updateTransaction(Long id, TransactionDTO.UpdateRequest request, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        if (request.getAmount() != null) transaction.setAmount(request.getAmount());
        if (request.getDate() != null) transaction.setDate(request.getDate());
        if (request.getCategory() != null) transaction.setCategory(request.getCategory());
        if (request.getType() != null) transaction.setType(request.getType());
        if (request.getDescription() != null) transaction.setDescription(request.getDescription());

        Transaction updated = transactionRepository.save(transaction);
        return toResponse(updated);
    }

    @Transactional
    public void deleteTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        transactionRepository.delete(transaction);
    }

    private TransactionDTO.TransactionResponse toResponse(Transaction transaction) {
        TransactionDTO.TransactionResponse response = new TransactionDTO.TransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setDate(transaction.getDate());
        response.setCategory(transaction.getCategory());
        response.setType(transaction.getType());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }
}
