package test.task_management.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.task_management.model.entity.OrderItem;

import java.time.LocalDate;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * P3 Requirement: Query the total booked quantity for an offering on a target booking date.
     * Excludes cancelled orders.
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
           "WHERE oi.offering.id = :offeringId " +
           "AND oi.order.bookingDate = :bookingDate " +
           "AND oi.order.status != test.task_management.model.enums.OrderStatus.CANCELLED")
    int countBookedQuantityByOfferingAndDate(
            @Param("offeringId") Long offeringId,
            @Param("bookingDate") LocalDate bookingDate
    );
}
