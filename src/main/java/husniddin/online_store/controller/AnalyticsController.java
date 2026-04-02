package husniddin.online_store.controller;

import husniddin.online_store.dto.response.AnalyticsResponse;
import husniddin.online_store.dto.response.ApiResponse;
import husniddin.online_store.enums.AnalyticsPeriod;
import husniddin.online_store.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'VIEWER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    @Operation(
            summary = "Get admin analytics",
            description = """
                    Returns aggregated metrics for the selected period.

                    - **DAILY** — today
                    - **WEEKLY** — last 7 days
                    - **MONTHLY** — last 30 days
                    - **CUSTOM** — requires `fromDate` and `toDate` (format: `yyyy-MM-dd`)
                    """
    )
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics(
            @Parameter(description = "Period type", example = "DAILY")
            @RequestParam(defaultValue = "DAILY") AnalyticsPeriod period,

            @Parameter(description = "Start date (CUSTOM only), format: yyyy-MM-dd", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "End date (CUSTOM only), format: yyyy-MM-dd", example = "2026-01-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @Parameter(description = "How many top-selling products to return (max 50)", example = "10")
            @RequestParam(defaultValue = "10") int topLimit
    ) {
        int safeLimit = Math.min(Math.max(topLimit, 1), 50);
        return ResponseEntity.ok(ApiResponse.success(
                analyticsService.getAnalytics(period, fromDate, toDate, safeLimit)));
    }
}
