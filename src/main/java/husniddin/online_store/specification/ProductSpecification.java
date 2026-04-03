package husniddin.online_store.specification;

import husniddin.online_store.entity.Product;
import javax.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ProductSpecification {

    private ProductSpecification() {}

    /**
     * Builds a JPA Specification that applies all active filters.
     * Null parameters are ignored (no predicate added).
     * Soft-delete is handled automatically by {@code @SQLRestriction} on the entity.
     */
    public static Specification<Product> withFilters(
            String search,
            Long categoryId,
            Long companyId,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                predicates.add(
                    cb.like(cb.lower(root.get("name")), "%" + search.trim().toLowerCase() + "%")
                );
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (companyId != null) {
                predicates.add(cb.equal(root.get("company").get("id"), companyId));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sellPrice"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sellPrice"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
