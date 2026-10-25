package test.task_management.model.response;

import org.springframework.http.HttpStatus;

public class ResponseBuilder {

    public static class Builder {
        private RestResponse<Object> restResponse;
        private AbstractResponse errorResponse;
        private HttpStatus status;

        public Builder() {
            this.status = HttpStatus.OK;
        }

        public Builder withData(Object data) {
            this.restResponse = new RestResponse(ResultCode.COMM_OPERATION_SUCCESS, "Operation success");
            this.restResponse.setPayLoad(data);
            return this;
        }

        public Builder success() {
            this.restResponse = new RestResponse(ResultCode.COMM_OPERATION_SUCCESS, "Operation success");
            return this;
        }
}
