package lfh.project.financetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lfh.project.financetracker.dto.request.*;
import lfh.project.financetracker.dto.response.AccountResponse;
import lfh.project.financetracker.dto.response.AuthResponse;
import lfh.project.financetracker.repository.AccountRepository;
import lfh.project.financetracker.repository.TransactionRepository;
import lfh.project.financetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private String authToken;
    private Long accountId1;
    private Long accountId2;

    @BeforeEach
    void setUp() throws Exception {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(),
                AuthResponse.class
        );
        authToken = authResponse.getToken();

        AccountCreateRequest account1 = AccountCreateRequest.builder()
                .name("Checking Account")
                .initialBalance(new BigDecimal("1000.00"))
                .build();

        MvcResult accountResult1 = mockMvc.perform(post("/accounts")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account1)))
                .andExpect(status().isOk())
                .andReturn();

        AccountResponse accountResponse1 = objectMapper.readValue(
                accountResult1.getResponse().getContentAsString(),
                AccountResponse.class
        );
        accountId1 = accountResponse1.getId();

        AccountCreateRequest account2 = AccountCreateRequest.builder()
                .name("Savings Account")
                .initialBalance(new BigDecimal("500.00"))
                .build();

        MvcResult accountResult2 = mockMvc.perform(post("/accounts")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account2)))
                .andExpect(status().isOk())
                .andReturn();

        AccountResponse accountResponse2 = objectMapper.readValue(
                accountResult2.getResponse().getContentAsString(),
                AccountResponse.class
        );
        accountId2 = accountResponse2.getId();
    }

    @Test
    void deposit_Success() throws Exception {
        DepositRequest request = DepositRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("100.00"))
                .description("Salary deposit")
                .build();

        mockMvc.perform(post("/transactions/deposit")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.description").value("Salary deposit"));

        mockMvc.perform(get("/accounts/" + accountId1)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1100.00));
    }

    @Test
    void deposit_NegativeAmount_BadRequest() throws Exception {
        DepositRequest request = DepositRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("-100.00"))
                .build();

        mockMvc.perform(post("/transactions/deposit")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_Success() throws Exception {
        WithdrawRequest request = WithdrawRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("200.00"))
                .description("ATM withdrawal")
                .build();

        mockMvc.perform(post("/transactions/withdraw")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.amount").value(200.00));

        mockMvc.perform(get("/accounts/" + accountId1)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800.00));
    }

    @Test
    void withdraw_InsufficientBalance_BadRequest() throws Exception {
        WithdrawRequest request = WithdrawRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("2000.00"))
                .build();

        mockMvc.perform(post("/transactions/withdraw")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_Success() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(accountId1)
                .toAccountId(accountId2)
                .amount(new BigDecimal("300.00"))
                .description("Transfer to savings")
                .build();

        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(300.00))
                .andExpect(jsonPath("$.toAccountId").value(accountId2));

        mockMvc.perform(get("/accounts/" + accountId1)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(700.00));

        mockMvc.perform(get("/accounts/" + accountId2)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800.00));
    }

    @Test
    void transfer_InsufficientBalance_BadRequest() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(accountId1)
                .toAccountId(accountId2)
                .amount(new BigDecimal("1500.00"))
                .build();

        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_SameAccount_BadRequest() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(accountId1)
                .toAccountId(accountId1)
                .amount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactionHistory_AllTransactions() throws Exception {
        DepositRequest deposit = DepositRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/transactions/deposit")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isOk());

        WithdrawRequest withdrawal = WithdrawRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("50.00"))
                .build();

        mockMvc.perform(post("/transactions/withdraw")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawal)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/transactions/history")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getTransactionHistory_FilterByAccountId() throws Exception {
        DepositRequest deposit1 = DepositRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/transactions/deposit")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit1)))
                .andExpect(status().isOk());

        DepositRequest deposit2 = DepositRequest.builder()
                .accountId(accountId2)
                .amount(new BigDecimal("200.00"))
                .build();

        mockMvc.perform(post("/transactions/deposit")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/transactions/history")
                        .header("Authorization", "Bearer " + authToken)
                        .param("accountId", accountId1.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].accountId").value(accountId1));
    }

    @Test
    void getTransactionHistory_FilterByType() throws Exception {
        DepositRequest deposit = DepositRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/transactions/deposit")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isOk());

        WithdrawRequest withdrawal = WithdrawRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("50.00"))
                .build();

        mockMvc.perform(post("/transactions/withdraw")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawal)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/transactions/history")
                        .header("Authorization", "Bearer " + authToken)
                        .param("type", "DEPOSIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
    }

    @Test
    void complexScenario_MultipleTransactions() throws Exception {
        DepositRequest deposit = DepositRequest.builder()
                .accountId(accountId1)
                .amount(new BigDecimal("500.00"))
                .description("Initial deposit")
                .build();
        mockMvc.perform(post("/transactions/deposit")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isOk());

        TransferRequest transfer = TransferRequest.builder()
                .fromAccountId(accountId1)
                .toAccountId(accountId2)
                .amount(new BigDecimal("300.00"))
                .description("Move to savings")
                .build();
        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isOk());

        WithdrawRequest withdrawal = WithdrawRequest.builder()
                .accountId(accountId2)
                .amount(new BigDecimal("100.00"))
                .description("Cash withdrawal")
                .build();
        mockMvc.perform(post("/transactions/withdraw")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawal)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/accounts/" + accountId1)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1200.00));

        mockMvc.perform(get("/accounts/" + accountId2)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(700.00));

        mockMvc.perform(get("/transactions/history")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }
}
