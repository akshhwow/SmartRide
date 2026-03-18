package com.smartride.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartride.entity.Transaction;
import com.smartride.entity.User;
import com.smartride.repository.TransactionRepository;
import com.smartride.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * TransactionService - provides access to user transaction history.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public List<Transaction> getUserTransactions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
