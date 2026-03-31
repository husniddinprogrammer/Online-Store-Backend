package husniddin.online_store.repository;

import husniddin.online_store.entity.FavoriteProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, Long> {

    Page<FavoriteProduct> findByUserId(Long userId, Pageable pageable);

    Optional<FavoriteProduct> findByUserIdAndProductId(Long userId, Long productId);

    /** Bypasses @SQLRestriction — returns the row even when is_deleted = true. */
    @Query(value = "SELECT * FROM favorite_products WHERE user_id = :userId AND product_id = :productId",
            nativeQuery = true)
    Optional<FavoriteProduct> findByUserIdAndProductIdIgnoreDeleted(
            @Param("userId") Long userId, @Param("productId") Long productId);
}
