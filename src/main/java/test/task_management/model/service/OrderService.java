package test.task_management.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.task_management.model.dto.OrderDto;
import test.task_management.model.entity.Offering;
import test.task_management.model.entity.Order;
import test.task_management.model.entity.OrderItem;
import test.task_management.model.enums.OrderStatus;
import test.task_management.model.exception.InactiveOfferingException;
import test.task_management.model.exception.ResourceNotFoundException;
import test.task_management.model.repository.OfferingRepository;
import test.task_management.model.repository.OrderRepository;
import test.task_management.model.request.BookOrderRequest;
import test.task_management.model.request.OrderItemRequest;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OfferingRepository offeringRepository;

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

            Offering offering = offeringRepository.findById(itemReq.getOfferingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Offering not found with id: " + itemReq.getOfferingId()));

            // P1 Requirement: Reject if offering is inactive
            if (!Boolean.TRUE.equals(offering.getActive())) {
                throw new InactiveOfferingException("Offering '" + offering.getName() + "' is inactive and cannot be booked");
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

