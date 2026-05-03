package com.utms.common.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/protected")
public class ProtectedController {

    @GetMapping("/me")
    public Map<String, String> me(Authentication authentication) {
        return Map.of("email", authentication.getName());
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public Map<String, String> student() {
        return Map.of("message", "student access granted");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> admin() {
        return Map.of("message", "admin access granted");
    }
}