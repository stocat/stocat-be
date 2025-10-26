package com.stocat.authapi.service.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
