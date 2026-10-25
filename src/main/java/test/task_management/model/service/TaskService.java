package test.task_management.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import test.task_management.model.dto.TaskDto;
import test.task_management.model.entity.Task;
import test.task_management.model.repository.TaskRepository;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskDto saveTask(TaskDto dto) {
        Task task = dto.toEntity();
        taskRepository.save(task);
        return task.toDto();
    }
}