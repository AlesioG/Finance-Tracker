package lfh.project.financetracker.dto.response;

import lfh.project.financetracker.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private Long accountId;
    private Long targetAccountId;
    private String description;
}