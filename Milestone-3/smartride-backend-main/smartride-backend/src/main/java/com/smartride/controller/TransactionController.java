package com.smartride.controller;

import com.smartride.dto.ApiResponse;
import com.smartride.entity.Transaction;
import com.smartride.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TransactionController - exposes transaction history endpoints.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactions(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Ensure users can only view their own transaction history
        Long currentUserId = ((com.smartride.entity.User) userDetails).getId();
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }

        List<Transaction> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved", transactions));
    }
}
