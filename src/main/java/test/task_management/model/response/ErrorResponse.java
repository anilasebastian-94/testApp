package test.task_management.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    @Builder.Default
    private Instant timestamp = Instant.now();
    private int status;
    private String error;
    private String message;
    private List<String> details;
}

