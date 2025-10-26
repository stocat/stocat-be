package com.stocat.authapi.service;

import com.stocat.authapi.config.JwtClaimKeys;
import com.stocat.authapi.config.JwtProvider;
import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.authapi.service.dto.*;
import com.stocat.common.mysql.domain.member.domain.AuthProvider;
import com.stocat.common.mysql.domain.member.domain.MemberRole;
import com.stocat.common.mysql.domain.member.domain.MemberStatus;
import com.stocat.common.mysql.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthFacadeTest {

    @Mock
    private AuthCommandService commandService;
    @Mock
    private AuthQueryService queryService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;

    private AuthFacade authFacade;

    @BeforeEach
    void setUp() {
        authFacade = new AuthFacade(commandService, queryService, passwordEncoder, jwtProvider);
    }

    @Test
    void 회원가입은_중복검사후_암호화된_비밀번호로_생성한다() {
        SignupRequest request = new SignupRequest("고냥이", "cat@stocat.com", "plain");
        when(queryService.existsByEmail(request.email())).thenReturn(false);
        when(queryService.existsByNickname(request.nickname())).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");

        authFacade.signup(request);

        ArgumentCaptor<CreateMemberCommand> captor = ArgumentCaptor.forClass(CreateMemberCommand.class);
        verify(commandService).create(captor.capture());
        CreateMemberCommand cmd = captor.getValue();
        assertThat(cmd.nickname()).isEqualTo("고냥이");
        assertThat(cmd.email()).isEqualTo("cat@stocat.com");
        assertThat(cmd.encodedPassword()).isEqualTo("encoded");
        verify(passwordEncoder).encode("plain");
    }

    @Test
    void 회원가입에서_이메일이_중복이면_예외를_던진다() {
        SignupRequest request = new SignupRequest("고냥이", "cat@stocat.com", "plain");
        when(queryService.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authFacade.signup(request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.EMAIL_ALREADY_EXISTS);
        verify(commandService, never()).create(any());
    }

    @Test
    void 회원가입에서_닉네임이_중복이면_예외를_던진다() {
        SignupRequest request = new SignupRequest("고냥이", "cat@stocat.com", "plain");
        when(queryService.existsByEmail(request.email())).thenReturn(false);
        when(queryService.existsByNickname(request.nickname())).thenReturn(true);

        assertThatThrownBy(() -> authFacade.signup(request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.NICKNAME_ALREADY_EXISTS);
        verify(commandService, never()).create(any());
    }

    @Test
    void 로그인은_비밀번호가_일치하면_토큰을_발급한다() {
        LoginRequest request = new LoginRequest("cat@stocat.com", "plain");
        MemberDto member = memberDto(1L, request.email(), "encoded", MemberRole.USER);
        when(queryService.getMemberByEmail(request.email())).thenReturn(member);
        when(passwordEncoder.matches("plain", "encoded")).thenReturn(true);
        when(jwtProvider.createAccessToken(anyString(), anyMap())).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(anyString())).thenReturn("refresh-token");

        AuthResponse response = authFacade.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(commandService).markLoginAt(1L);

        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(jwtProvider).createAccessToken(eq("1"), claimsCaptor.capture());
        assertThat(claimsCaptor.getValue()).containsEntry(JwtClaimKeys.EMAIL, request.email());
        assertThat(claimsCaptor.getValue()).containsEntry(JwtClaimKeys.ROLE, MemberRole.USER.name());
        verify(jwtProvider).createRefreshToken("1");
    }

    @Test
    void 로그인시_비밀번호가_틀리면_예외를_던진다() {
        LoginRequest request = new LoginRequest("cat@stocat.com", "plain");
        MemberDto member = memberDto(1L, request.email(), "encoded", MemberRole.USER);
        when(queryService.getMemberByEmail(request.email())).thenReturn(member);
        when(passwordEncoder.matches("plain", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authFacade.login(request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_CREDENTIALS);
        verify(commandService, never()).markLoginAt(anyLong());
        verify(jwtProvider, never()).createAccessToken(any(), anyMap());
    }

    @Test
    void 로그인시_DB비밀번호가_null이면_예외를_던진다() {
        LoginRequest request = new LoginRequest("cat@stocat.com", "plain");
        MemberDto member = memberDto(1L, request.email(), null, MemberRole.USER);
        when(queryService.getMemberByEmail(request.email())).thenReturn(member);

        assertThatThrownBy(() -> authFacade.login(request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_CREDENTIALS);
    }

    private MemberDto memberDto(Long id, String email, String password, MemberRole role) {
        return new MemberDto(
                id,
                "고냥이",
                email,
                password,
                AuthProvider.LOCAL,
                "",
                MemberStatus.ACTIVE,
                role,
                null,
                null,
                null,
                null
        );
    }
}
