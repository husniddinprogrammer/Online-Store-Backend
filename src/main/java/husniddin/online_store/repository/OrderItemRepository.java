package husniddin.online_store.repository;

import husniddin.online_store.entity.OrderItem;
import husniddin.online_store.entity.Product;
import husniddin.online_store.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Distinct products the user received (DELIVERED) but has NOT yet commented on.
     * Uses NOT EXISTS subquery — single round-trip, no N+1.
     */
    @Query("SELECT DISTINCT oi.product " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.user.id = :userId " +
           "AND o.status = husniddin.online_store.enums.OrderStatus.DELIVERED " +
           "AND NOT EXISTS (" +
           "  SELECT 1 FROM Comment c " +
           "  WHERE c.user.id = :userId " +
           "  AND c.product.id = oi.product.id" +
           ")")
    Page<Product> findDeliveredUnreviewedProducts(@Param("userId") Long userId, Pageable pageable);

    // ── Analytics ─────────────────────────────────────────────────────────────

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.order.status <> :excluded AND oi.order.createdAt BETWEEN :from AND :to")
    Long sumSoldQuantity(@Param("excluded") OrderStatus excluded,
                         @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Top selling products by total quantity sold in the period, pageable for top-N. */
    @Query(value = "SELECT oi.product_id AS product_id, " +
                   "p.name AS name, " +
                   "SUM(oi.quantity) AS total_sold, " +
                   "SUM(oi.price * oi.quantity) AS revenue " +
                   "FROM order_items oi " +
                   "JOIN products p ON p.id = oi.product_id " +
                   "JOIN orders o ON o.id = oi.order_id " +
                   "WHERE o.is_deleted = false " +
                   "AND oi.is_deleted = false " +
                   "AND p.is_deleted = false " +
                   "AND o.status <> 'CANCELLED' " +
                   "AND o.created_at BETWEEN :from AND :to " +
                   "GROUP BY oi.product_id, p.name " +
                   "ORDER BY total_sold DESC",
           nativeQuery = true)
    List<Object[]> findTopSellingProducts(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          Pageable pageable);
}
