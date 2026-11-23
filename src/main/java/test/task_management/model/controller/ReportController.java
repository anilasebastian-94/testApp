package test.task_management.model.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import test.task_management.model.dto.DailyCollectionReportDto;
import test.task_management.model.response.RestResponse;
import test.task_management.model.service.ReportService;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * P4 Requirement: Daily collection report for a temple and date.
     * Totals match to the paisa. Days counted in Indian Standard Time (IST).
     */
    @GetMapping("/daily-collection")
    public ResponseEntity<RestResponse<DailyCollectionReportDto>> getDailyCollectionReport(
            @RequestParam(required = false) LocalDate date
    ) {
        DailyCollectionReportDto report = reportService.getDailyCollectionReport(date);
        return ResponseEntity.ok(RestResponse.ok(report));
    }
}
