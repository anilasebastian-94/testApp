package test.task_management.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.task_management.model.entity.Order;
import test.task_management.model.enums.OrderStatus;

import java.time.LocalDate;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE " +
            "(:phoneNumber IS NULL OR o.phoneNumber = :phoneNumber) AND " +
            "(:bookingDate IS NULL OR o.bookingDate = :bookingDate) AND " +
            "(:status IS NULL OR o.status = :status)")
    Page<Order> searchOrders(
            @Param("phoneNumber") String phoneNumber,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("status") OrderStatus status,
            Pageable pageable);
}
