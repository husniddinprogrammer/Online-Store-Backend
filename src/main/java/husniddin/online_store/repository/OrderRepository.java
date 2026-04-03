package husniddin.online_store.repository;

import husniddin.online_store.entity.Order;
import husniddin.online_store.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    // ── Analytics ─────────────────────────────────────────────────────────────

    /** Sum profit across all non-deleted, non-cancelled orders in the period. */
    @Query("SELECT SUM(o.totalProfit) FROM Order o WHERE o.status <> :excluded AND o.createdAt BETWEEN :from AND :to")
    BigDecimal sumTotalProfit(@Param("excluded") OrderStatus excluded,
                              @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Sum revenue across all non-deleted, non-cancelled orders in the period. */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status <> :excluded AND o.createdAt BETWEEN :from AND :to")
    BigDecimal sumTotalRevenue(@Param("excluded") OrderStatus excluded,
                               @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Count all non-deleted orders in the period (any status). */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    long countOrdersInPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Returns rows of [date::String, revenue::BigDecimal] for chart rendering (all non-deleted, non-cancelled). */
    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM-DD') AS chart_date, " +
                   "COALESCE(SUM(total_amount), 0) AS revenue " +
                   "FROM orders " +
                   "WHERE status <> 'CANCELLED' " +
                   "AND is_deleted = false " +
                   "AND created_at BETWEEN :from AND :to " +
                   "GROUP BY TO_CHAR(created_at, 'YYYY-MM-DD') " +
                   "ORDER BY chart_date",
           nativeQuery = true)
    List<Object[]> getRevenueChartData(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
