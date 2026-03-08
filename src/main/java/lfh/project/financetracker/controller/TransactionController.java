package lfh.project.financetracker.controller;

import lfh.project.financetracker.dto.request.DepositRequest;
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

import java.time.LocalDate;
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
    public ResponseEntity<List<TransactionResponse>> getHistory(
            Authentication authentication,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                transactionService.getHistory(authentication.getName(), type, startDate, endDate)
        );
    }
}