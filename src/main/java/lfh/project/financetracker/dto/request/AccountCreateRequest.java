package lfh.project.financetracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    @PositiveOrZero
    private BigDecimal initialBalance;
}