package lfh.project.financetracker.controller;

import lfh.project.financetracker.dto.request.AccountCreateRequest;
import lfh.project.financetracker.dto.request.AccountUpdateRequest;
import lfh.project.financetracker.dto.response.AccountResponse;
import lfh.project.financetracker.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            Authentication authentication,
            @Valid @RequestBody AccountCreateRequest request
    ) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(accountService.createAccount(userEmail, request));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(accountService.getAllAccounts(userEmail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(accountService.getAccountById(userEmail, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request
    ) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(accountService.updateAccount(userEmail, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String userEmail = authentication.getName();
        accountService.deleteAccount(userEmail, id);
        return ResponseEntity.noContent().build();
    }
}