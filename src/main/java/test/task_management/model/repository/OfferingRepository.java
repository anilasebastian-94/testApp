package test.task_management.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.task_management.model.entity.Offering;

import java.util.List;

public interface OfferingRepository extends JpaRepository<Offering, Long> {
    List<Offering> findByActiveTrue();
}
