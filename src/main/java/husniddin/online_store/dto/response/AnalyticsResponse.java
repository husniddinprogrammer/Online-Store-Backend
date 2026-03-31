package husniddin.online_store.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AnalyticsResponse implements Serializable {

    private String period;
    private LocalDate fromDate;
    private LocalDate toDate;

    private BigDecimal totalProfit;
    private BigDecimal totalRevenue;
    private long totalSoldProductsCount;
    private long totalOrdersCount;
    private long totalUsersAdded;
    private long totalProductsAdded;

    private List<TopSellingProductDto> topSellingProducts;
    private List<RevenueChartPointDto> revenueChart;
}
