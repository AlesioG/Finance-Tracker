package lfh.project.financetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class AccountResponse {

    private Long id;
    private String name;
    private BigDecimal balance;
}