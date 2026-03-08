package lfh.project.financetracker.dto.request;

import lfh.project.financetracker.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionFilterRequest {

    private Long accountId;
    private TransactionType type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
