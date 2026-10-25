package test.task_management.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import test.task_management.model.dto.TaskDto;
import test.task_management.model.enums.TaskStatus;

@Entity
@Getter
@Setter
@Table(name = "task")
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    public TaskDto toDto() {
        TaskDto taskDto = new TaskDto();
        taskDto.setId(id);
        taskDto.setName(name);
        taskDto.setDescription(description);
        taskDto.setStatus(status);
        return taskDto;
    }


}
