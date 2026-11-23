package test.task_management.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.task_management.model.dto.OfferingCollectionSummaryDto;
import test.task_management.model.entity.OrderItem;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * P3 Requirement: Query the total booked quantity for an offering on a target booking date.
     * Excludes cancelled orders so cancelled orders automatically free the daily quota (P4).
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
           "WHERE oi.offering.id = :offeringId " +
           "AND oi.order.bookingDate = :bookingDate " +
           "AND oi.order.status != test.task_management.model.enums.OrderStatus.CANCELLED")
    int countBookedQuantityByOfferingAndDate(
            @Param("offeringId") Long offeringId,
            @Param("bookingDate") LocalDate bookingDate
    );

    /**
     * P4 Requirement: Financial daily collection aggregation by offering.
     * Excludes cancelled orders and groups counts & amounts within IST boundaries.
     */
    @Query("SELECT new test.task_management.model.dto.OfferingCollectionSummaryDto(" +
           "oi.offering.id, oi.offering.name, SUM(oi.quantity), SUM(oi.subtotal)) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status != test.task_management.model.enums.OrderStatus.CANCELLED " +
           "AND oi.order.createdAt >= :startInstant AND oi.order.createdAt < :endInstant " +
           "GROUP BY oi.offering.id, oi.offering.name")
    List<OfferingCollectionSummaryDto> getDailyOfferingSummary(
            @Param("startInstant") Instant startInstant,
            @Param("endInstant") Instant endInstant
    );
}
