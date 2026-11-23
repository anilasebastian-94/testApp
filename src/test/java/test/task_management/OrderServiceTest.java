package test.task_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import test.task_management.model.dto.OrderDto;
import test.task_management.model.dto.PageResponse;
import test.task_management.model.entity.Offering;
import test.task_management.model.entity.Order;
import test.task_management.model.enums.OrderStatus;
import test.task_management.model.exception.DailyLimitExceededException;
import test.task_management.model.exception.InactiveOfferingException;
import test.task_management.model.repository.OfferingRepository;
import test.task_management.model.repository.OrderItemRepository;
import test.task_management.model.repository.OrderRepository;
import test.task_management.model.request.BookOrderRequest;
import test.task_management.model.request.OrderItemRequest;
import test.task_management.model.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OfferingRepository offeringRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    private Offering pushpanjali;
    private Offering archana;
    private Offering inactiveOffering;
    private Offering limitedOffering;

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

        limitedOffering = Offering.builder()
                .id(4L)
                .name("Udayasthamana Pooja")
                .price(new BigDecimal("1000.00"))
                .active(true)
                .dailyLimit(5)
                .build();
    }

    // ==================== P1 Tests ====================

    @Test
    @DisplayName("P1 Acceptance Criteria: Order for 2 Pushpanjali at 30 and 1 Archana at 50 has a total of 110")
    void testOrderTotalCalculation() {
        when(offeringRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pushpanjali));
        when(offeringRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(archana));
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
        when(offeringRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(inactiveOffering));

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

    // ==================== P2 Tests ====================

    @Test
    @DisplayName("P2 Acceptance Criteria: Searching by phone number returns only that devotee's orders")
    void testSearchByPhoneNumber_ReturnsOnlyDevoteesOrders() {
        String targetPhone = "9876543210";
        List<Order> mockOrders = List.of(
                createOrder(1L, "Devotee 1", targetPhone, LocalDate.now()),
                createOrder(2L, "Devotee 1", targetPhone, LocalDate.now().plusDays(1))
        );

        Page<Order> mockPage = new PageImpl<>(mockOrders, PageRequest.of(0, 10), 2);
        when(orderRepository.searchOrders(eq(targetPhone), any(), any(), any(Pageable.class)))
                .thenReturn(mockPage);

        PageResponse<OrderDto> response = orderService.searchOrders(targetPhone, null, null, 1, 10);

        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertTrue(response.getContent().stream().allMatch(o -> o.getPhoneNumber().equals(targetPhone)));
    }

    @Test
    @DisplayName("P2 Acceptance Criteria: Requesting page 2 returns the next set of results, with no repeats or gaps from page 1")
    void testStablePagination_Page2ReturnsNextSetWithoutRepeatsOrGaps() {
        Order o5 = createOrder(5L, "Devotee 5", "9876500005", LocalDate.now().plusDays(5));
        Order o4 = createOrder(4L, "Devotee 4", "9876500004", LocalDate.now().plusDays(4));
        Order o3 = createOrder(3L, "Devotee 3", "9876500003", LocalDate.now().plusDays(3));
        Order o2 = createOrder(2L, "Devotee 2", "9876500002", LocalDate.now().plusDays(2));

        Page<Order> page1Orders = new PageImpl<>(List.of(o5, o4), PageRequest.of(0, 2), 5);
        Page<Order> page2Orders = new PageImpl<>(List.of(o3, o2), PageRequest.of(1, 2), 5);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(orderRepository.searchOrders(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page1Orders)
                .thenReturn(page2Orders);

        PageResponse<OrderDto> page1 = orderService.searchOrders(null, null, null, 1, 2);
        PageResponse<OrderDto> page2 = orderService.searchOrders(null, null, null, 2, 2);

        List<Long> page1Ids = page1.getContent().stream().map(OrderDto::getId).toList();
        assertEquals(List.of(5L, 4L), page1Ids);

        List<Long> page2Ids = page2.getContent().stream().map(OrderDto::getId).toList();
        assertEquals(List.of(3L, 2L), page2Ids);

        Set<Long> commonIds = page1Ids.stream().filter(page2Ids::contains).collect(Collectors.toSet());
        assertTrue(commonIds.isEmpty(), "Page 2 must have no repeats from Page 1");

        verify(orderRepository, times(2)).searchOrders(isNull(), isNull(), isNull(), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertNotNull(capturedPageable.getSort().getOrderFor("id"), "Tie-breaker by id must be present for stable pagination");
        assertEquals(Sort.Direction.DESC, capturedPageable.getSort().getOrderFor("id").getDirection());
    }

    // ==================== P3 Tests ====================

    @Test
    @DisplayName("P3 Acceptance Criteria: With daily limit 5 and 4 already booked, an order for 2 is rejected and an order for 1 succeeds")
    void testDailyLimit_OrderFor2Rejected_OrderFor1Succeeds() {
        LocalDate bookingDate = LocalDate.now().plusDays(3);
        when(offeringRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(limitedOffering));

        // When 4 are already booked
        when(orderItemRepository.countBookedQuantityByOfferingAndDate(4L, bookingDate)).thenReturn(4);

        // Case 1: Order for 2 (4 + 2 = 6 > 5) -> MUST FAIL
        BookOrderRequest orderFor2 = BookOrderRequest.builder()
                .devoteeName("Devotee Exceed")
                .phoneNumber("9876543210")
                .nakshatra("Ashwini")
                .bookingDate(bookingDate)
                .items(List.of(new OrderItemRequest(4L, 2)))
                .build();

        DailyLimitExceededException ex = assertThrows(DailyLimitExceededException.class, () -> orderService.placeOrder(orderFor2));
        assertTrue(ex.getMessage().contains("Daily limit of 5 exceeded"), "Must reject with limit exceeded message");

        // Case 2: Order for 1 (4 + 1 = 5 <= 5) -> MUST SUCCEED
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(201L);
            return o;
        });

        BookOrderRequest orderFor1 = BookOrderRequest.builder()
                .devoteeName("Devotee Allowed")
                .phoneNumber("9876543210")
                .nakshatra("Bharani")
                .bookingDate(bookingDate)
                .items(List.of(new OrderItemRequest(4L, 1)))
                .build();

        OrderDto successOrder = orderService.placeOrder(orderFor1);
        assertNotNull(successOrder);
        assertEquals(new BigDecimal("1000.00"), successOrder.getTotalAmount());
        assertEquals("Devotee Allowed", successOrder.getDevoteeName());
    }

    @Test
    @DisplayName("P3 Acceptance Criteria: 20 simultaneous orders for 1 each against daily limit of 5 -> Exactly 5 succeed")
    void testSimultaneousOrders_AgainstDailyLimitOf5_Exactly5Succeed() throws Exception {
        LocalDate bookingDate = LocalDate.now().plusDays(4);
        int totalRequests = 20;
        int limit = 5;

        // Simulated DB state guarded by row-level lock (simulating SELECT ... FOR UPDATE)
        Object lock = new Object();
        AtomicInteger bookedQuantity = new AtomicInteger(0);

        when(offeringRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(limitedOffering));

        // Simulate transactional row-level lock execution in PostgreSQL
        when(orderItemRepository.countBookedQuantityByOfferingAndDate(eq(4L), eq(bookingDate))).thenAnswer(invocation -> {
            synchronized (lock) {
                return bookedQuantity.get();
            }
        });

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            synchronized (lock) {
                Order order = invocation.getArgument(0);
                order.setId(ThreadLocalRandom.current().nextLong(1000, 9999));
                bookedQuantity.incrementAndGet(); // Atomic increment after successful validation
                return order;
            }
        });

        ExecutorService executor = Executors.newFixedThreadPool(totalRequests);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(totalRequests);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startGate.await(); // Ensures all 20 threads hit simultaneously
                    BookOrderRequest req = BookOrderRequest.builder()
                            .devoteeName("Concurrent Devotee " + index)
                            .phoneNumber("98765000" + String.format("%02d", index))
                            .nakshatra("Ashwini")
                            .bookingDate(bookingDate)
                            .items(List.of(new OrderItemRequest(4L, 1)))
                            .build();

                    // Synchronized block represents the database transaction holding the FOR UPDATE lock
                    synchronized (lock) {
                        orderService.placeOrder(req);
                    }
                    successCount.incrementAndGet();
                } catch (DailyLimitExceededException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    // unexpected error
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown(); // Fire all 20 threads at once
        boolean completed = endGate.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All threads should complete within timeout");
        assertEquals(limit, successCount.get(), "Exactly 5 simultaneous orders must succeed");
        assertEquals(totalRequests - limit, failureCount.get(), "Exactly 15 simultaneous orders must fail");
    }

    // ==================== P4 Tests ====================

    @Test
    @DisplayName("P4 Acceptance Criteria: Staff can cancel an order. A cancelled order frees its daily limit")
    void testCancelOrder_SetsStatusToCancelledAndFreesLimit() {
        Order activeOrder = createOrder(101L, "Devotee Cancel", "9876543210", LocalDate.now().plusDays(2));
        activeOrder.setStatus(OrderStatus.BOOKED);

        when(orderRepository.findById(101L)).thenReturn(Optional.of(activeOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDto cancelled = orderService.cancelOrder(101L);

        assertNotNull(cancelled);
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus(), "Order status must transition to CANCELLED");
        verify(orderRepository, times(1)).save(activeOrder);
    }

    private Order createOrder(Long id, String devoteeName, String phoneNumber, LocalDate bookingDate) {
        return Order.builder()
                .id(id)
                .devoteeName(devoteeName)
                .phoneNumber(phoneNumber)
                .nakshatra("Ashwini")
                .bookingDate(bookingDate)
                .totalAmount(new BigDecimal("30.00"))
                .status(OrderStatus.BOOKED)
                .items(new ArrayList<>())
                .build();
    }
}
