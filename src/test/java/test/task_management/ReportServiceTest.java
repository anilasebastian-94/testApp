package test.task_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import test.task_management.model.dto.DailyCollectionReportDto;
import test.task_management.model.dto.OfferingCollectionSummaryDto;
import test.task_management.model.repository.OrderItemRepository;
import test.task_management.model.service.ReportService;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ReportService reportService;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Test
    @DisplayName("P4 Acceptance Criteria: Order placed at 00:15 IST on a date is counted on that date, totals match to the paisa")
    void testDailyCollectionReport_ISTBoundariesAndPaisaAccuracy() {
        LocalDate reportDate = LocalDate.of(2026, 11, 20);

        // Expected IST boundaries
        Instant expectedStart = reportDate.atStartOfDay(IST).toInstant();
        Instant expectedEnd = reportDate.plusDays(1).atStartOfDay(IST).toInstant();

        // 00:15 IST order falls into expected window
        ZonedDateTime orderTimeIST = ZonedDateTime.of(2026, 11, 20, 0, 15, 0, 0, IST);
        Instant orderInstant = orderTimeIST.toInstant();

        assertTrue(orderInstant.isAfter(expectedStart) && orderInstant.isBefore(expectedEnd),
                "00:15 IST must fall within reportDate boundaries in IST");

        // Mock aggregated report items
        List<OfferingCollectionSummaryDto> mockSummaries = List.of(
                new OfferingCollectionSummaryDto(1L, "Pushpanjali", 2L, new BigDecimal("60.00")),
                new OfferingCollectionSummaryDto(2L, "Archana", 1L, new BigDecimal("50.00")),
                new OfferingCollectionSummaryDto(4L, "Udayasthamana Pooja", 1L, new BigDecimal("1000.00"))
        );

        when(orderItemRepository.getDailyOfferingSummary(eq(expectedStart), eq(expectedEnd)))
                .thenReturn(mockSummaries);

        DailyCollectionReportDto report = reportService.getDailyCollectionReport(reportDate);

        assertNotNull(report);
        assertEquals(reportDate, report.getReportDate());
        assertEquals("Asia/Kolkata (IST)", report.getTimezone());

        // P4 Acceptance Criteria: Report total equals the sum of its offerings' totals to the exact paisa
        BigDecimal expectedTotal = new BigDecimal("1110.00");
        assertEquals(expectedTotal, report.getTotalAmount(), "Total must match the sum of offering subtotals to the paisa");

        BigDecimal sumOfSubtotals = report.getOfferings().stream()
                .map(OfferingCollectionSummaryDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(report.getTotalAmount(), sumOfSubtotals, "Report total must equal the sum of offerings' totals");
    }
}

