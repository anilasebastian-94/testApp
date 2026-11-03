package test.task_management.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import test.task_management.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String devoteeName;
    private String phoneNumber;
    private String nakshatra;
    private LocalDate bookingDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Instant createdAt;
    private List<OrderItemDto> items;
}

