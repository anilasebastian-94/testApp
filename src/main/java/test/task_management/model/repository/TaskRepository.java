package test.task_management.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.task_management.model.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
