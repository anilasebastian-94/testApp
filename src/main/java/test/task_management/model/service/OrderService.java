package test.task_management.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.task_management.model.dto.OrderDto;
import test.task_management.model.dto.PageResponse;
import test.task_management.model.entity.Offering;
import test.task_management.model.entity.Order;
import test.task_management.model.entity.OrderItem;
import test.task_management.model.enums.OrderStatus;
import test.task_management.model.exception.DailyLimitExceededException;
import test.task_management.model.exception.InactiveOfferingException;
import test.task_management.model.exception.ResourceNotFoundException;
import test.task_management.model.repository.OfferingRepository;
import test.task_management.model.repository.OrderItemRepository;
import test.task_management.model.repository.OrderRepository;
import test.task_management.model.request.BookOrderRequest;
import test.task_management.model.request.OrderItemRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OfferingRepository offeringRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public OrderDto placeOrder(BookOrderRequest request) {
        validateOrderRequest(request);

        Order order = Order.builder()
                .devoteeName(request.getDevoteeName().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .nakshatra(request.getNakshatra().trim())
                .bookingDate(request.getBookingDate())
                .status(OrderStatus.BOOKED)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getOfferingId() == null) {
                throw new IllegalArgumentException("Offering id is required");
            }
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be at least 1");
            }

            // P3: Acquire pessimistic write lock (SELECT ... FOR UPDATE) on the offering row
            Offering offering = offeringRepository.findByIdForUpdate(itemReq.getOfferingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Offering not found with id: " + itemReq.getOfferingId()));

            // P1 Requirement: Reject if offering is inactive
            if (!Boolean.TRUE.equals(offering.getActive())) {
                throw new InactiveOfferingException("Offering '" + offering.getName() + "' is inactive and cannot be booked");
            }

            // P3 Requirement: Enforce daily limit under concurrency lock
            if (offering.getDailyLimit() != null) {
                int alreadyBooked = orderItemRepository.countBookedQuantityByOfferingAndDate(offering.getId(), request.getBookingDate());
                if (alreadyBooked + itemReq.getQuantity() > offering.getDailyLimit()) {
                    throw new DailyLimitExceededException(
                            String.format("Daily limit of %d exceeded for offering '%s'. Already booked: %d, Requested: %d",
                                    offering.getDailyLimit(), offering.getName(), alreadyBooked, itemReq.getQuantity())
                    );
                }
            }

            BigDecimal itemSubtotal = offering.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            calculatedTotal = calculatedTotal.add(itemSubtotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .offering(offering)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(offering.getPrice())
                    .subtotal(itemSubtotal)
                    .build();

            order.getItems().add(orderItem);
        }

        // Server calculates the order total
        order.setTotalAmount(calculatedTotal);

        Order saved = orderRepository.save(order);
        return saved.toDto();
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return order.toDto();
    }

    /**
     * P2 Requirement: List orders with filters and deterministic stable pagination.
     * Common searches use DB composite indexes without full table scans.
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderDto> searchOrders(String phoneNumber, LocalDate bookingDate, OrderStatus status, int page, int size) {
        int pageIndex = page > 0 ? page - 1 : 0;
        int pageSize = size > 0 ? Math.min(size, 100) : 10;

        Sort stableSort = Sort.by(Sort.Direction.DESC, "bookingDate")
                .and(Sort.by(Sort.Direction.DESC, "id"));

        Pageable pageable = PageRequest.of(pageIndex, pageSize, stableSort);
        String trimmedPhone = (phoneNumber != null && !phoneNumber.trim().isEmpty()) ? phoneNumber.trim() : null;

        Page<Order> orderPage = orderRepository.searchOrders(trimmedPhone, bookingDate, status, pageable);
        Page<OrderDto> dtoPage = orderPage.map(Order::toDto);

        return PageResponse.from(dtoPage);
    }

    /**
     * P4 Requirement: Staff can cancel an order.
     * A cancelled order immediately frees its daily limit because countBookedQuantityByOfferingAndDate
     * ignores cancelled orders.
     */
    @Transactional
    public OrderDto cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return order.toDto();
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updated = orderRepository.save(order);
        return updated.toDto();
    }

    private void validateOrderRequest(BookOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request cannot be null");
        }
        if (request.getDevoteeName() == null || request.getDevoteeName().trim().isEmpty()) {
            throw new IllegalArgumentException("Devotee name is required");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (request.getNakshatra() == null || request.getNakshatra().trim().isEmpty()) {
            throw new IllegalArgumentException("Nakshatra (birth star) is required");
        }
        if (request.getBookingDate() == null) {
            throw new IllegalArgumentException("Booking date is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one offering item must be selected");
        }
    }
}
