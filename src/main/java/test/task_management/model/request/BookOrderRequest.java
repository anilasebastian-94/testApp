package test.task_management.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookOrderRequest {
    private String devoteeName;
    private String phoneNumber;
    private String nakshatra;
    private LocalDate bookingDate;
    private List<OrderItemRequest> items;
}

