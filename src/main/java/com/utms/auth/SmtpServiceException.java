package com.utms.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Raised when the OTP email cannot be delivered because the SMTP service is unreachable
 * (UC 1.3 Exception 4b - SMTP Service Failure). Maps to HTTP 503 so the client can show
 * "Service temporarily unavailable. Please try again later."
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class SmtpServiceException extends RuntimeException {
    public SmtpServiceException(String message) {
        super(message);
    }
}
