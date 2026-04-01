package husniddin.online_store.repository;

import husniddin.online_store.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByProductId(Long productId, Pageable pageable);

    Page<Comment> findByUserId(Long userId, Pageable pageable);

    Optional<Comment> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.product.id = :productId AND c.isDeleted = false")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * Returns true if the user has at least one PAID or DELIVERED order that contains this product.
     * Single JOIN query — no N+1.
     */
    @Query("""
            SELECT COUNT(oi) > 0
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.user.id    = :userId
              AND oi.product.id = :productId
              AND o.status IN ('PAID', 'DELIVERED')
            """)
    boolean existsVerifiedPurchase(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * Returns true if the user has at least one DELIVERED order containing this product.
     */
    @Query("""
            SELECT COUNT(oi) > 0
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.user.id    = :userId
              AND oi.product.id = :productId
              AND o.status      = husniddin.online_store.enums.OrderStatus.DELIVERED
            """)
    boolean existsDeliveredPurchase(@Param("userId") Long userId, @Param("productId") Long productId);
}
