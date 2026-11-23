package test.task_management.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferingCollectionSummaryDto {
    private Long offeringId;
    private String offeringName;
    private Long count;
    private BigDecimal subtotal;
}

