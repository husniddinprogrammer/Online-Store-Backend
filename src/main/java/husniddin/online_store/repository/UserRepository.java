package husniddin.online_store.repository;

import husniddin.online_store.entity.User;
import husniddin.online_store.enums.Role;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    /** Used by admins: excludes roles the caller must not see. */
    @Query("SELECT u FROM User u WHERE " +
           "u.role NOT IN :excludedRoles AND " +
           "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', cast(:search as String), '%')) " +
           "OR LOWER(u.surname) LIKE LOWER(CONCAT('%', cast(:search as String), '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', cast(:search as String), '%')))")
    Page<User> searchUsersExcluding(
            @Param("search") String search,
            @Param("excludedRoles") Collection<Role> excludedRoles,
            Pageable pageable);

    /** Used by notification broadcast: find all active users with a given role. */
    List<User> findByRoleAndIsDeletedFalseAndBlockedFalse(Role role);

    /** Acquires a row-level lock on the user row — use for balance mutations to prevent race conditions. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailForUpdate(@Param("email") String email);
}
