package lfh.project.financetracker.controller;

import lfh.project.financetracker.dto.request.DepositRequest;
import lfh.project.financetracker.dto.request.TransactionFilterRequest;
import lfh.project.financetracker.dto.request.TransferRequest;
import lfh.project.financetracker.dto.request.WithdrawRequest;
import lfh.project.financetracker.dto.response.TransactionResponse;
import lfh.project.financetracker.entity.TransactionType;
import lfh.project.financetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            Authentication authentication,
            @Valid @RequestBody DepositRequest request
    ) {
        return ResponseEntity.ok(
                transactionService.deposit(authentication.getName(), request)
        );
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            Authentication authentication,
            @Valid @RequestBody WithdrawRequest request
    ) {
        return ResponseEntity.ok(
                transactionService.withdraw(authentication.getName(), request)
        );
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request
    ) {
        return ResponseEntity.ok(
                transactionService.transfer(authentication.getName(), request)
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            Authentication authentication,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        String userEmail = authentication.getName();

        TransactionFilterRequest filter = TransactionFilterRequest.builder()
                .accountId(accountId)
                .type(type)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return ResponseEntity.ok(transactionService.getHistory(userEmail, filter));
    }
}