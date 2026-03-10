package lfh.project.financetracker.dto.request;

import lfh.project.financetracker.entity.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFilterRequest {

    private Long accountId;
    private TransactionType type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
