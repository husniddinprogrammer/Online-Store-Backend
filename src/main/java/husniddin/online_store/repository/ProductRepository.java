package husniddin.online_store.repository;

import husniddin.online_store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByCompanyId(Long companyId, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    long countByCreatedAtBetween(java.time.LocalDateTime from, java.time.LocalDateTime to);
}
