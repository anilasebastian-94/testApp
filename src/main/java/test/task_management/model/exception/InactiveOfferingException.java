package test.task_management.model.exception;

import org.springframework.http.HttpStatus;

public class InactiveOfferingException extends ApiException {
    public InactiveOfferingException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

