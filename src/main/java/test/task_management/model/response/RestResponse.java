package test.task_management.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestResponse<T> {
    @Builder.Default
    private boolean success = true;
    private String message;
    private T data;
    @Builder.Default
    private Instant timestamp = Instant.now();

    public static <T> RestResponse<T> ok(T data) {
        return RestResponse.<T>builder()
                .success(true)
                .message("Operation successful")
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> RestResponse<T> ok(String message, T data) {
        return RestResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }
}
