package test.task_management.model.request;

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
public class CreateOfferingRequest {
    private String name;
    private BigDecimal price;
    @Builder.Default
    private Boolean active = true;
}

