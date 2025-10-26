package com.stocat.authapi.controller;

import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.authapi.service.AuthFacade;
import com.stocat.authapi.service.dto.AuthErrorItem;
import com.stocat.authapi.service.dto.AuthResponse;
import com.stocat.authapi.service.dto.LoginRequest;
import com.stocat.authapi.service.dto.SignupRequest;
import com.stocat.common.mysql.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "회원 가입 / 로그인 API")
public class AuthController {

    private final AuthFacade authFacade;

    public AuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @PostMapping("/signup")
    @Operation(summary = "회원 가입", description = "이메일/닉네임 중복 검증 후 로컬 회원을 생성합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "가입 완료")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "중복 이메일 등")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authFacade.signup(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호를 검증하고 액세스/리프레시 토큰을 발급합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "자격 증명 오류")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authFacade.login(request)));
    }

    @GetMapping("/errors")
    @Operation(summary = "오류 코드 목록", description = "AuthErrorCode 코드/메시지 목록 반환")
    public ResponseEntity<ApiResponse<List<AuthErrorItem>>> errors() {
        List<AuthErrorItem> items = Arrays.stream(AuthErrorCode.values())
                .map(e -> new AuthErrorItem(e.code(), e.message()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(items));
    }
}
