package test.task_management.model.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import test.task_management.model.dto.OrderDto;
import test.task_management.model.request.BookOrderRequest;
import test.task_management.model.response.RestResponse;
import test.task_management.model.service.OrderService;

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
}

