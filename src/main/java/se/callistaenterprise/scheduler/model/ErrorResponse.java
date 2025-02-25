package se.callistaenterprise.scheduler.model;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private int httpStatusCode;
    private String message;

    public ErrorResponse(int httpStatusCode, String message) {
        super();
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }
}
