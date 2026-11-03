package test.task_management.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import test.task_management.model.entity.Offering;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferingDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Boolean active;

    public Offering toEntity() {
        return Offering.builder()
                .id(id)
                .name(name)
                .price(price)
                .active(active != null ? active : true)
                .build();
    }
}
