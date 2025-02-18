package se.callistaenterprise.scheduler.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;

public class BadRequestException extends HttpStatusCodeException {

    public BadRequestException(String statusText) {
        super(HttpStatusCode.valueOf(400), statusText, null, null, null);
    }
}
