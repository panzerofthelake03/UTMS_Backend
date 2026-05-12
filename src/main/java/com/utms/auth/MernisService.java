package com.utms.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
public class MernisService {

    @Value("${app.mernis.simulate-unavailable:false}")
    private boolean simulateUnavailable;

    public void verifyIdentity(String tcIdentityNumber, String firstName, String lastName, LocalDate dateOfBirth) {
        if (simulateUnavailable) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Verification Service Unavailable. Please try again later.");
        }
        // Real MERNIS integration would go here
    }
}
