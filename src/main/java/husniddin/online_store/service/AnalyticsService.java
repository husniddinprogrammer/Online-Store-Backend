package husniddin.online_store.service;

import husniddin.online_store.dto.response.AnalyticsResponse;
import husniddin.online_store.dto.response.RevenueChartPointDto;
import husniddin.online_store.dto.response.TopSellingProductDto;
import husniddin.online_store.enums.AnalyticsPeriod;
import husniddin.online_store.enums.OrderStatus;
import husniddin.online_store.exception.BadRequestException;
import husniddin.online_store.repository.OrderItemRepository;
import husniddin.online_store.repository.OrderRepository;
import husniddin.online_store.repository.ProductRepository;
import husniddin.online_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Returns aggregated analytics for the requested period.
     * Results are cached per period + date range to avoid repeated DB hits.
     * Cache name is "analytics:{PERIOD}" so TTL is configured per-period in RedisCacheConfig.
     */
    @Caching(cacheable = {
        @Cacheable(value = "analytics:DAILY",   condition = "#period.name() == 'DAILY'",   key = "'daily'"),
        @Cacheable(value = "analytics:WEEKLY",  condition = "#period.name() == 'WEEKLY'",  key = "'weekly'"),
        @Cacheable(value = "analytics:MONTHLY", condition = "#period.name() == 'MONTHLY'", key = "'monthly'"),
        @Cacheable(value = "analytics:CUSTOM",  condition = "#period.name() == 'CUSTOM'",  key = "#fromDate + ':' + #toDate + ':' + #topLimit")
    })
    public AnalyticsResponse getAnalytics(AnalyticsPeriod period, LocalDate fromDate, LocalDate toDate, int topLimit) {
        if (period == AnalyticsPeriod.CUSTOM) {
            if (fromDate == null || toDate == null) {
                throw new BadRequestException("fromDate and toDate are required for CUSTOM period");
            }
            if (fromDate.isAfter(toDate)) {
                throw new BadRequestException("fromDate must not be after toDate");
            }
        }

        LocalDateTime[] range = resolveDateRange(period, fromDate, toDate);
        LocalDateTime from = range[0];
        LocalDateTime to   = range[1];

        BigDecimal totalProfit  = nullSafe(orderRepository.sumTotalProfit(OrderStatus.CANCELLED, from, to));
        BigDecimal totalRevenue = nullSafe(orderRepository.sumTotalRevenue(OrderStatus.CANCELLED, from, to));
        long totalOrders        = orderRepository.countOrdersInPeriod(from, to);
        long totalSold          = nullSafeLong(orderItemRepository.sumSoldQuantity(OrderStatus.CANCELLED, from, to));
        long usersAdded         = userRepository.countByCreatedAtBetween(from, to);
        long productsAdded      = productRepository.countByCreatedAtBetween(from, to);

        List<TopSellingProductDto> topProducts = fetchTopProducts(from, to, topLimit);
        List<RevenueChartPointDto> chart       = fetchRevenueChart(from, to);

        return AnalyticsResponse.builder()
                .period(period.name())
                .fromDate(from.toLocalDate())
                .toDate(to.toLocalDate())
                .totalProfit(totalProfit)
                .totalRevenue(totalRevenue)
                .totalOrdersCount(totalOrders)
                .totalSoldProductsCount(totalSold)
                .totalUsersAdded(usersAdded)
                .totalProductsAdded(productsAdded)
                .topSellingProducts(topProducts)
                .revenueChart(chart)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private LocalDateTime[] resolveDateRange(AnalyticsPeriod period, LocalDate fromDate, LocalDate toDate) {
        LocalDate today = LocalDate.now();
        return switch (period) {
            case DAILY   -> new LocalDateTime[]{today.atStartOfDay(), today.atTime(LocalTime.MAX)};
            case WEEKLY  -> new LocalDateTime[]{today.minusDays(6).atStartOfDay(), today.atTime(LocalTime.MAX)};
            case MONTHLY -> new LocalDateTime[]{today.minusDays(29).atStartOfDay(), today.atTime(LocalTime.MAX)};
            case CUSTOM  -> new LocalDateTime[]{fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX)};
        };
    }

    private List<TopSellingProductDto> fetchTopProducts(LocalDateTime from, LocalDateTime to, int limit) {
        List<Object[]> rows = orderItemRepository.findTopSellingProducts(
                from, to, PageRequest.of(0, limit));
        return rows.stream()
                .map(row -> new TopSellingProductDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        new BigDecimal(row[3].toString())
                ))
                .toList();
    }

    private List<RevenueChartPointDto> fetchRevenueChart(LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = orderRepository.getRevenueChartData(from, to);
        return rows.stream()
                .map(row -> new RevenueChartPointDto(
                        LocalDate.parse((String) row[0]),
                        new BigDecimal(row[1].toString())
                ))
                .toList();
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private long nullSafeLong(Long value) {
        return value != null ? value : 0L;
    }
}
