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

    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.product.id = :productId AND c.isDeleted = false")
    Double findAverageRatingByProductId(@Param("productId") Long productId);
}
