package husniddin.online_store.repository;

import husniddin.online_store.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    long countByUserIdAndIsSeenFalse(Long userId);

    /** Bulk-update: marks all unseen notifications for a user as seen in one query. */
    @Modifying
    @Query("UPDATE Notification n SET n.isSeen = true WHERE n.user.id = :userId AND n.isSeen = false")
    void markAllSeenByUserId(@Param("userId") Long userId);
}
