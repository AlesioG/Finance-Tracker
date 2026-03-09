package lfh.project.financetracker.service;

import jakarta.persistence.EntityNotFoundException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Account testAccount;
    private Account testAccount2;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testAccount = Account.builder()
                .id(1L)
                .name("Checking Account")
                .balance(new BigDecimal("1000.00"))
                .user(testUser)
                .build();

        testAccount2 = Account.builder()
                .id(2L)
                .name("Savings Account")
                .balance(new BigDecimal("500.00"))
                .user(testUser)
                .build();
    }

    @Test
    void deposit_Success() {
        DepositRequest request = DepositRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("100.00"))
                .description("Test deposit")
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .timestamp(LocalDateTime.now())
                .account(testAccount)
                .description("Test deposit")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponse response = transactionService.deposit("test@example.com", request);

        assertNotNull(response);
        assertEquals(TransactionType.DEPOSIT, response.getType());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals("Test deposit", response.getDescription());
        verify(accountRepository).save(testAccount);
        assertEquals(new BigDecimal("1100.00"), testAccount.getBalance());
    }

    @Test
    void deposit_NegativeAmount_ThrowsException() {
        DepositRequest request = DepositRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("-100.00"))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.deposit("test@example.com", request));
    }

    @Test
    void deposit_ZeroAmount_ThrowsException() {
        DepositRequest request = DepositRequest.builder()
                .accountId(1L)
                .amount(BigDecimal.ZERO)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.deposit("test@example.com", request));
    }

    @Test
    void deposit_AccountNotFound_ThrowsException() {
        DepositRequest request = DepositRequest.builder()
                .accountId(999L)
                .amount(new BigDecimal("100.00"))
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> transactionService.deposit("test@example.com", request));
    }

    @Test
    void deposit_UserNotFound_ThrowsException() {
        DepositRequest request = DepositRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("100.00"))
                .build();

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> transactionService.deposit("nonexistent@example.com", request));
    }

    @Test
    void withdraw_Success() {
        WithdrawRequest request = WithdrawRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("100.00"))
                .description("Test withdrawal")
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.WITHDRAWAL)
                .timestamp(LocalDateTime.now())
                .account(testAccount)
                .description("Test withdrawal")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponse response = transactionService.withdraw("test@example.com", request);

        assertNotNull(response);
        assertEquals(TransactionType.WITHDRAWAL, response.getType());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        verify(accountRepository).save(testAccount);
        assertEquals(new BigDecimal("900.00"), testAccount.getBalance());
    }

    @Test
    void withdraw_InsufficientBalance_ThrowsException() {
        WithdrawRequest request = WithdrawRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("2000.00"))
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAccount));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.withdraw("test@example.com", request));

        assertTrue(exception.getMessage().contains("Insufficient balance"));
    }

    @Test
    void withdraw_NegativeAmount_ThrowsException() {
        WithdrawRequest request = WithdrawRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("-50.00"))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.withdraw("test@example.com", request));
    }

    @Test
    void transfer_Success() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("200.00"))
                .description("Test transfer")
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("200.00"))
                .type(TransactionType.TRANSFER)
                .timestamp(LocalDateTime.now())
                .account(testAccount)
                .targetAccount(testAccount2)
                .description("Test transfer")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(testAccount2));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponse response = transactionService.transfer("test@example.com", request);

        assertNotNull(response);
        assertEquals(TransactionType.TRANSFER, response.getType());
        assertEquals(new BigDecimal("200.00"), response.getAmount());
        assertEquals(testAccount2.getId(), response.getToAccountId());
        verify(accountRepository, times(2)).save(any(Account.class));
        assertEquals(new BigDecimal("800.00"), testAccount.getBalance());
        assertEquals(new BigDecimal("700.00"), testAccount2.getBalance());
    }

    @Test
    void transfer_InsufficientBalance_ThrowsException() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("2000.00"))
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(testAccount2));

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.transfer("test@example.com", request));
    }

    @Test
    void transfer_SameAccount_ThrowsException() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountId(1L)
                .amount(new BigDecimal("100.00"))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.transfer("test@example.com", request));
    }

    @Test
    void transfer_ToAccountNotOwnedByUser_ThrowsException() {
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .build();

        Account otherAccount = Account.builder()
                .id(3L)
                .name("Other Account")
                .balance(new BigDecimal("500.00"))
                .user(otherUser)
                .build();

        TransferRequest request = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountId(3L)
                .amount(new BigDecimal("100.00"))
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> transactionService.transfer("test@example.com", request));
    }

    @Test
    void getTransactionHistory_AllTransactions_Success() {
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .timestamp(LocalDateTime.now())
                .account(testAccount)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(2L)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.WITHDRAWAL)
                .timestamp(LocalDateTime.now())
                .account(testAccount)
                .build();

        TransactionFilterRequest filter = TransactionFilterRequest.builder().build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserIdWithFilters(eq(1L), eq(null), eq(null), eq(null)))
                .thenReturn(Arrays.asList(transaction1, transaction2));

        List<TransactionResponse> response = transactionService.getHistory("test@example.com", filter);

        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    void getTransactionHistory_FilterByAccountId_Success() {
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .timestamp(LocalDateTime.now())
                .account(testAccount)
                .build();

        TransactionFilterRequest filter = TransactionFilterRequest.builder()
                .accountId(1L)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByAccountIdWithFilters(eq(1L), eq(null), eq(null), eq(null)))
                .thenReturn(Arrays.asList(transaction1));

        List<TransactionResponse> response = transactionService.getHistory("test@example.com", filter);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void getTransactionHistory_FilterByType_Success() {
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .timestamp(LocalDateTime.now())
                .account(testAccount)
                .build();

        TransactionFilterRequest filter = TransactionFilterRequest.builder()
                .type(TransactionType.DEPOSIT)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserIdWithFilters(eq(1L), eq(TransactionType.DEPOSIT), eq(null), eq(null)))
                .thenReturn(Arrays.asList(transaction1));

        List<TransactionResponse> response = transactionService.getHistory("test@example.com", filter);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(TransactionType.DEPOSIT, response.get(0).getType());
    }

    @Test
    void getTransactionHistory_FilterByDateRange_Success() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .timestamp(LocalDateTime.now().minusDays(3))
                .account(testAccount)
                .build();

        TransactionFilterRequest filter = TransactionFilterRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserIdWithFilters(eq(1L), eq(null), eq(startDate), eq(endDate)))
                .thenReturn(Arrays.asList(transaction1));

        List<TransactionResponse> response = transactionService.getHistory("test@example.com", filter);

        assertNotNull(response);
        assertEquals(1, response.size());
    }
}
