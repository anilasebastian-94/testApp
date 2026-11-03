package test.task_management.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import test.task_management.model.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
