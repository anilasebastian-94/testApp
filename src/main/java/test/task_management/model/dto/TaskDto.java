package test.task_management.model.dto;

import lombok.Getter;
import lombok.Setter;
import test.task_management.model.entity.Task;
import test.task_management.model.enums.TaskStatus;

@Getter
@Setter
public class TaskDto {
    private Long id;
    private String name;
    private String description;
    private TaskStatus status;

    public Task toEntity() {
        Task task = new Task();
        task.setId(id);
        task.setName(name);
        task.setDescription(description);
        task.setStatus(status);
        return task;
    }
}
