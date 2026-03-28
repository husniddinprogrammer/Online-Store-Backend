package husniddin.online_store.repository;

import husniddin.online_store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', cast(:search as String), '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:companyId IS NULL OR p.company.id = :companyId) AND " +
           "(:minPrice IS NULL OR p.sellPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.sellPrice <= :maxPrice)")
    Page<Product> findWithFilters(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("companyId") Long companyId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByCompanyId(Long companyId, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}
