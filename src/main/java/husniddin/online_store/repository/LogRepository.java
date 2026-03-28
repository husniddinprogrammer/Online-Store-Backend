package husniddin.online_store.repository;

import husniddin.online_store.entity.Log;
import husniddin.online_store.enums.LogAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    Page<Log> findByEntityName(String entityName, Pageable pageable);
    Page<Log> findByEntityNameAndEntityId(String entityName, Long entityId, Pageable pageable);
    Page<Log> findByAction(LogAction action, Pageable pageable);
}
