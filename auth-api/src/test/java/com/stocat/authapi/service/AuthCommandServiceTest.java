package com.stocat.authapi.service;

import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.authapi.service.dto.CreateMemberCommand;
import com.stocat.common.mysql.domain.member.domain.AuthProvider;
import com.stocat.common.mysql.domain.member.domain.Member;
import com.stocat.common.mysql.domain.member.domain.MemberRole;
import com.stocat.common.mysql.domain.member.domain.MemberStatus;
import com.stocat.common.mysql.domain.member.repository.MemberRepository;
import com.stocat.common.mysql.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthCommandServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private AuthCommandService commandService;

    @BeforeEach
    void setUp() {
        commandService = new AuthCommandService(memberRepository);
    }

    @Test
    void 회원생성은_Command를_엔터티로_저장한다() {
        CreateMemberCommand cmd = new CreateMemberCommand(
                "고냥이",
                "cat@stocat.com",
                "encoded",
                AuthProvider.LOCAL,
                "pid",
                MemberStatus.ACTIVE,
                MemberRole.USER
        );
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Member created = commandService.create(cmd);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        Member saved = captor.getValue();
        assertThat(saved.getNickname()).isEqualTo("고냥이");
        assertThat(saved.getEmail()).isEqualTo("cat@stocat.com");
        assertThat(saved.getPassword()).isEqualTo("encoded");
        assertThat(created).isSameAs(saved);
    }

    @Test
    void 로그인기록은_시간을_갱신하고_저장한다() {
        Member member = Member.create("고냥이", "cat@stocat.com", "encoded", AuthProvider.LOCAL, "pid", MemberStatus.ACTIVE, MemberRole.USER);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        commandService.markLoginAt(1L);

        assertThat(member.getLastLoginAt()).isNotNull();
    }

    @Test
    void 로그인기록중_회원이_없으면_예외를_던진다() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandService.markLoginAt(1L))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_NOT_FOUND);
    }
}
