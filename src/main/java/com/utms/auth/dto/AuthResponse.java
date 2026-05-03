package com.utms.auth.dto;

import java.util.List;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email,
        String firstName,
        String lastName,
        List<String> roles
) {}

