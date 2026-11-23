package test.task_management.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCollectionReportDto {
    private LocalDate reportDate;
    private String timezone;
    private BigDecimal totalAmount;
    private List<OfferingCollectionSummaryDto> offerings;
}

