package test.task_management.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.task_management.model.dto.DailyCollectionReportDto;
import test.task_management.model.dto.OfferingCollectionSummaryDto;
import test.task_management.model.repository.OrderItemRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderItemRepository orderItemRepository;
    public static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    /**
     * P4 Requirement: Daily collection report for a date with total amount and count per offering.
     * Accurately counted in Indian Standard Time (IST). Orders placed at 00:15 IST are counted on that date.
     * Cancelled orders are excluded. Totals match to the paisa.
     */
    @Transactional(readOnly = true)
    public DailyCollectionReportDto getDailyCollectionReport(LocalDate date) {
        if (date == null) {
            date = LocalDate.now(IST_ZONE);
        }

        // IST Day Boundaries
        Instant startOfDay = date.atStartOfDay(IST_ZONE).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(IST_ZONE).toInstant();

        List<OfferingCollectionSummaryDto> offerings = orderItemRepository.getDailyOfferingSummary(startOfDay, endOfDay);

        // Sum subtotals to match exact paisa
        BigDecimal totalAmount = offerings.stream()
                .map(OfferingCollectionSummaryDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DailyCollectionReportDto.builder()
                .reportDate(date)
                .timezone("Asia/Kolkata (IST)")
                .totalAmount(totalAmount)
                .offerings(offerings)
                .build();
    }
}

