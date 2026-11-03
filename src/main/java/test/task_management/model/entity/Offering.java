package test.task_management.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import test.task_management.model.dto.OfferingDto;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "offerings")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Offering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    public OfferingDto toDto() {
        return OfferingDto.builder()
                .id(id)
                .name(name)
                .price(price)
                .active(active)
                .build();
    }
}

