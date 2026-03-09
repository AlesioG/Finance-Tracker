package lfh.project.financetracker.service;

import lfh.project.financetracker.dto.request.DepositRequest;
import lfh.project.financetracker.dto.request.TransactionFilterRequest;
import lfh.project.financetracker.dto.request.TransferRequest;
import lfh.project.financetracker.dto.request.WithdrawRequest;
import lfh.project.financetracker.dto.response.TransactionResponse;
import lfh.project.financetracker.entity.Account;
import lfh.project.financetracker.entity.Transaction;
import lfh.project.financetracker.entity.TransactionType;
import lfh.project.financetracker.entity.User;
import lfh.project.financetracker.repository.AccountRepository;
import lfh.project.financetracker.repository.TransactionRepository;
import lfh.project.financetracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionResponse deposit(String userEmail, DepositRequest request) {
        log.info("Deposit requested for account {} amount {}", request.getAccountId(), request.getAmount());
        validateAmount(request.getAmount());

        User user = getUserByEmail(userEmail);
        Account account = getOwnedAccount(user.getId(), request.getAccountId());

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .timestamp(LocalDateTime.now())
                .account(account)
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Transactional
    public TransactionResponse withdraw(String userEmail, WithdrawRequest request) {
        log.info("Withdrawal requested for account {} amount {}", request.getAccountId(), request.getAmount());
        validateAmount(request.getAmount());

        User user = getUserByEmail(userEmail);
        Account account = getOwnedAccount(user.getId(), request.getAccountId());

        validateSufficientBalance(account, request.getAmount());

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .timestamp(LocalDateTime.now())
                .account(account)
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Transactional
    public TransactionResponse transfer(String userEmail, TransferRequest request) {
        log.info("Transfer requested from account {} to account {} amount {}",
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount());
        validateAmount(request.getAmount());
        validateDifferentAccounts(request.getFromAccountId(), request.getToAccountId());

        User user = getUserByEmail(userEmail);

        Account fromAccount = getOwnedAccount(user.getId(), request.getFromAccountId());
        Account toAccount = getOwnedAccount(user.getId(), request.getToAccountId());

        validateSufficientBalance(fromAccount, request.getAmount());

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = Transaction.builder()
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .timestamp(LocalDateTime.now())
                .account(fromAccount)
                .targetAccount(toAccount)
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    public List<TransactionResponse> getHistory(
            String userEmail,
            TransactionFilterRequest filter
    ) {
        User user = getUserByEmail(userEmail);

        List<Transaction> transactions;

        if (filter.getAccountId() != null) {
            getOwnedAccount( user.getId(),filter.getAccountId());

            transactions = transactionRepository.findByAccountIdWithFilters(
                    filter.getAccountId(),
                    filter.getType(),
                    filter.getStartDate(),
                    filter.getEndDate()
            );
        } else {
            transactions = transactionRepository.findByUserIdWithFilters(
                    user.getId(),
                    filter.getType(),
                    filter.getStartDate(),
                    filter.getEndDate()
            );
        }

        return transactions.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance. Available: " +
                    account.getBalance() + ", Required: " + amount);
        }
    }

    private void validateDifferentAccounts(Long fromAccountId, Long toAccountId) {
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private Account getOwnedAccount(Long userId, Long accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .timestamp(transaction.getTimestamp())
                .accountId(transaction.getAccount().getId())
                .toAccountId(
                        transaction.getTargetAccount() != null ? transaction.getTargetAccount().getId() : null
                )
                .description(transaction.getDescription())
                .build();
    }
}