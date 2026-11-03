package test.task_management.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import test.task_management.model.dto.OrderDto;
import test.task_management.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "devotee_name", nullable = false)
    private String devoteeName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "nakshatra", nullable = false)
    private String nakshatra;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.BOOKED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = OrderStatus.BOOKED;
        }
    }

    public OrderDto toDto() {
        return OrderDto.builder()
                .id(id)
                .devoteeName(devoteeName)
                .phoneNumber(phoneNumber)
                .nakshatra(nakshatra)
                .bookingDate(bookingDate)
                .totalAmount(totalAmount)
                .status(status)
                .createdAt(createdAt)
                .items(items != null ? items.stream().map(OrderItem::toDto).toList() : List.of())
                .build();
    }
}

