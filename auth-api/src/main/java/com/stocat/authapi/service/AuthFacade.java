package com.stocat.authapi.service;

import com.stocat.authapi.config.JwtClaimKeys;
import com.stocat.authapi.config.JwtProvider;
import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.authapi.service.dto.*;
import com.stocat.common.mysql.domain.member.domain.AuthProvider;
import com.stocat.common.mysql.domain.member.domain.MemberRole;
import com.stocat.common.mysql.domain.member.domain.MemberStatus;
import com.stocat.common.mysql.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthCommandService commandService;
    private final AuthQueryService queryService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * Use case: signup
     *
     * @param request 회원가입 req
     */
    public void signup(SignupRequest request) {
        if (queryService.existsByEmail(request.email())) {
            throw new ApiException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (queryService.existsByNickname(request.nickname())) {
            throw new ApiException(AuthErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        String encoded = passwordEncoder.encode(request.password());
        var cmd = new CreateMemberCommand(
                request.nickname(),
                request.email(),
                encoded,
                AuthProvider.LOCAL,
                "",
                MemberStatus.ACTIVE,
                MemberRole.USER
        );
        commandService.create(cmd);
    }

    /**
     * Use case: login
     *
     * @param request 로그인 req
     * @return AuthResponse(유저 토큰)
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        MemberDto member = queryService.getMemberByEmail(request.email());

        if (member.password() == null || !passwordEncoder.matches(request.password(), member.password())) {
            throw new ApiException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        commandService.markLoginAt(member.id());

        String access = jwtProvider.createAccessToken(
                String.valueOf(member.id()),
                Map.of(
                        JwtClaimKeys.EMAIL, member.email(),
                        JwtClaimKeys.ROLE, member.role().name()
                )
        );
        String refresh = jwtProvider.createRefreshToken(String.valueOf(member.id()));
        return new AuthResponse(access, refresh);
    }
}
