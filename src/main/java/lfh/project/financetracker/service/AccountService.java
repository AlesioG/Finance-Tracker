package lfh.project.financetracker.service;

import lfh.project.financetracker.dto.request.AccountCreateRequest;
import lfh.project.financetracker.dto.request.AccountUpdateRequest;
import lfh.project.financetracker.dto.response.AccountResponse;
import lfh.project.financetracker.entity.Account;
import lfh.project.financetracker.entity.User;
import lfh.project.financetracker.repository.AccountRepository;
import lfh.project.financetracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountResponse createAccount(String userEmail, AccountCreateRequest request) {
        User user = getUserByEmail(userEmail);

        Account account = Account.builder()
                .name(request.getName())
                .balance(request.getInitialBalance())
                .user(user)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Creating account '{}' with initial balance {}", request.getName(), request.getInitialBalance());
        return mapToResponse(savedAccount);
    }

    public List<AccountResponse> getAllAccounts(String userEmail) {
        User user = getUserByEmail(userEmail);

        return accountRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AccountResponse getAccountById(String userEmail, Long accountId) {
        User user = getUserByEmail(userEmail);

        Account account = accountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        return mapToResponse(account);
    }

    public AccountResponse updateAccount(String userEmail, Long accountId, AccountUpdateRequest request) {
        User user = getUserByEmail(userEmail);

        Account account = accountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        account.setName(request.getName());

        Account updatedAccount = accountRepository.save(account);
        log.info("Updating account {}", accountId);
        return mapToResponse(updatedAccount);
    }

    public void deleteAccount(String userEmail, Long accountId) {
        User user = getUserByEmail(userEmail);

        Account account = accountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        log.info("Deleting account {}", accountId);
        accountRepository.delete(account);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .balance(account.getBalance())
                .build();
    }
}