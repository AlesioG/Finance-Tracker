package lfh.project.financetracker.service;


import lfh.project.financetracker.dto.request.AccountCreateRequest;
import lfh.project.financetracker.dto.request.AccountUpdateRequest;
import lfh.project.financetracker.entity.Account;
import lfh.project.financetracker.entity.Role;
import lfh.project.financetracker.entity.User;
import lfh.project.financetracker.repository.AccountRepository;
import lfh.project.financetracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_shouldCreateAccountForAuthenticatedUser() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.USER)
                .build();

        AccountCreateRequest request = new AccountCreateRequest();
        request.setName("Checking");
        request.setInitialBalance(new BigDecimal("500.00"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(10L);
            return account;
        });

        var response = accountService.createAccount("test@example.com", request);

        assertEquals("Checking", response.getName());
        assertEquals(new BigDecimal("500.00"), response.getBalance());
        assertEquals(10L, response.getId());
    }

    @Test
    void getAllAccounts_shouldReturnOnlyUsersAccounts() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.USER)
                .build();

        Account account = Account.builder()
                .id(10L)
                .name("Checking")
                .balance(new BigDecimal("300.00"))
                .user(user)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(account));

        var result = accountService.getAllAccounts("test@example.com");

        assertEquals(1, result.size());
        assertEquals("Checking", result.get(0).getName());
    }

    @Test
    void getAccountById_shouldThrowException_whenAccountDoesNotBelongToUser() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> accountService.getAccountById("test@example.com", 999L)
        );
    }

    @Test
    void updateAccount_shouldChangeName() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.USER)
                .build();

        Account account = Account.builder()
                .id(10L)
                .name("Old Name")
                .balance(new BigDecimal("100.00"))
                .user(user)
                .build();

        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setName("New Name");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = accountService.updateAccount("test@example.com", 10L, request);

        assertEquals("New Name", response.getName());
    }

    @Test
    void createAccount_shouldThrowException_whenUserNotFound() {
        AccountCreateRequest request = new AccountCreateRequest();
        request.setName("Checking");
        request.setInitialBalance(new BigDecimal("100.00"));

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> accountService.createAccount("missing@example.com", request)
        );
    }
}