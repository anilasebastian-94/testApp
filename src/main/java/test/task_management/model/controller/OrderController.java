package test.task_management.model.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import test.task_management.model.dto.OrderDto;
import test.task_management.model.dto.PageResponse;
import test.task_management.model.enums.OrderStatus;
import test.task_management.model.request.BookOrderRequest;
import test.task_management.model.response.RestResponse;
import test.task_management.model.service.OrderService;

import java.time.LocalDate;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<RestResponse<OrderDto>> bookOrder(@RequestBody BookOrderRequest request) {
        OrderDto createdOrder = orderService.placeOrder(request);
        return new ResponseEntity<>(RestResponse.ok("Order placed successfully", createdOrder), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<OrderDto>> getOrderById(@PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(RestResponse.ok(order));
    }

    /**
     * P2 Requirement: List orders filtered by date, status, and devotee phone number.
     * Returned in pages with stable order.
     */
    @GetMapping
    public ResponseEntity<RestResponse<PageResponse<OrderDto>>> searchOrders(
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<OrderDto> orders = orderService.searchOrders(phoneNumber, date, status, page, size);
        return ResponseEntity.ok(RestResponse.ok(orders));
    }
}
