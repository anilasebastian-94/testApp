package test.task_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import test.task_management.model.dto.OrderDto;
import test.task_management.model.entity.Offering;
import test.task_management.model.entity.Order;
import test.task_management.model.exception.InactiveOfferingException;
import test.task_management.model.repository.OfferingRepository;
import test.task_management.model.repository.OrderRepository;
import test.task_management.model.request.BookOrderRequest;
import test.task_management.model.request.OrderItemRequest;
import test.task_management.model.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OfferingRepository offeringRepository;

    @InjectMocks
    private OrderService orderService;

    private Offering pushpanjali;
    private Offering archana;
    private Offering inactiveOffering;

    @BeforeEach
    void setUp() {
        pushpanjali = Offering.builder()
                .id(1L)
                .name("Pushpanjali")
                .price(new BigDecimal("30.00"))
                .active(true)
                .build();

        archana = Offering.builder()
                .id(2L)
                .name("Archana")
                .price(new BigDecimal("50.00"))
                .active(true)
                .build();

        inactiveOffering = Offering.builder()
                .id(3L)
                .name("Special Pooja")
                .price(new BigDecimal("100.00"))
                .active(false)
                .build();
    }

    @Test
    @DisplayName("P1 Acceptance Criteria: Order for 2 Pushpanjali at 30 and 1 Archana at 50 has a total of 110")
    void testOrderTotalCalculation() {
        when(offeringRepository.findById(1L)).thenReturn(Optional.of(pushpanjali));
        when(offeringRepository.findById(2L)).thenReturn(Optional.of(archana));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        BookOrderRequest request = BookOrderRequest.builder()
                .devoteeName("Arun Kumar")
                .phoneNumber("9876543210")
                .nakshatra("Ashwini")
                .bookingDate(LocalDate.now().plusDays(2))
                .items(List.of(
                        new OrderItemRequest(1L, 2), // 2 * 30 = 60
                        new OrderItemRequest(2L, 1)  // 1 * 50 = 50
                ))
                .build();

        OrderDto result = orderService.placeOrder(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("110.00"), result.getTotalAmount(), "Total must be 110.00 (2 * 30 + 1 * 50)");
        assertEquals("Arun Kumar", result.getDevoteeName());
        assertEquals("9876543210", result.getPhoneNumber());
        assertEquals("Ashwini", result.getNakshatra());
        assertEquals(2, result.getItems().size());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("P1 Acceptance Criteria: An order for an inactive offering is rejected with InactiveOfferingException")
    void testInactiveOfferingRejection() {
        when(offeringRepository.findById(3L)).thenReturn(Optional.of(inactiveOffering));

        BookOrderRequest request = BookOrderRequest.builder()
                .devoteeName("Devotee Test")
                .phoneNumber("9876543210")
                .nakshatra("Bharani")
                .bookingDate(LocalDate.now().plusDays(1))
                .items(List.of(
                        new OrderItemRequest(3L, 1)
                ))
                .build();

        InactiveOfferingException ex = assertThrows(InactiveOfferingException.class, () -> orderService.placeOrder(request));
        assertTrue(ex.getMessage().contains("inactive"), "Exception message must state offering is inactive");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("P1 Requirement: Order rejected when required fields are missing")
    void testOrderMissingRequiredFields() {
        BookOrderRequest missingPhone = BookOrderRequest.builder()
                .devoteeName("Devotee Test")
                .nakshatra("Bharani")
                .bookingDate(LocalDate.now().plusDays(1))
                .items(List.of(new OrderItemRequest(1L, 1)))
                .build();

        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(missingPhone));
    }
}

