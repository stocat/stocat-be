package com.stocat.authapi.service.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank(message = "닉네임은 필수 값입니다.")
        String nickname,
        @NotBlank(message = "이메일은 필수 값입니다.")
        String email,
        @NotBlank(message = "비밀번호는 필수 값입니다.")
        String password
) {
}
