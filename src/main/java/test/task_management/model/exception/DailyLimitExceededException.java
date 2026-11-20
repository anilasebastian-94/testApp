package test.task_management.model.exception;

import org.springframework.http.HttpStatus;

public class DailyLimitExceededException extends ApiException {
    public DailyLimitExceededException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
