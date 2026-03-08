package lfh.project.financetracker.service;

import lfh.project.financetracker.dto.request.DepositRequest;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionResponse deposit(String userEmail, DepositRequest request) {
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
        User user = getUserByEmail(userEmail);
        Account account = getOwnedAccount(user.getId(), request.getAccountId());

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

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
        User user = getUserByEmail(userEmail);

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        Account fromAccount = getOwnedAccount(user.getId(), request.getFromAccountId());
        Account toAccount = getOwnedAccount(user.getId(), request.getToAccountId());

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

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
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    ) {
        User user = getUserByEmail(userEmail);

        List<Transaction> transactions;

        if (type != null && startDate != null && endDate != null) {
            transactions = transactionRepository.findByAccountUserIdAndTypeAndTimestampBetweenOrderByTimestampDesc(
                    user.getId(),
                    type,
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            );
        } else if (type != null) {
            transactions = transactionRepository.findByAccountUserIdAndTypeOrderByTimestampDesc(
                    user.getId(),
                    type
            );
        } else if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByAccountUserIdAndTimestampBetweenOrderByTimestampDesc(
                    user.getId(),
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            );
        } else {
            transactions = transactionRepository.findByAccountUserIdOrderByTimestampDesc(user.getId());
        }

        return transactions.stream()
                .map(this::mapToResponse)
                .toList();
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
                .targetAccountId(
                        transaction.getTargetAccount() != null ? transaction.getTargetAccount().getId() : null
                )
                .description(transaction.getDescription())
                .build();
    }
}