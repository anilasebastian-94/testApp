package test.task_management.model.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import test.task_management.model.dto.TaskDto;
import test.task_management.model.service.TaskService;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/save")
    public ResponseEntity<TaskDto> saveTask(TaskDto taskDto) {
        taskService.saveTask(taskDto);
        return ResponseEntity.ok(taskDto);
    }
}
