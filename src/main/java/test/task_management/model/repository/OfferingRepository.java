package test.task_management.model.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.task_management.model.entity.Offering;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferingRepository extends JpaRepository<Offering, Long> {
    List<Offering> findByActiveTrue();

    /**
     * P3 Requirement: Pessimistic write lock (SELECT ... FOR UPDATE) on the offering row.
     * Prevents race conditions during simultaneous online and counter booking requests.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Offering o WHERE o.id = :id")
    Optional<Offering> findByIdForUpdate(@Param("id") Long id);
}
