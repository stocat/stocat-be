package com.stocat.authapi.service;

import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.authapi.service.dto.CreateMemberCommand;
import com.stocat.common.mysql.domain.member.domain.Member;
import com.stocat.common.mysql.domain.member.domain.MemberStatus;
import com.stocat.common.mysql.domain.member.repository.MemberRepository;
import com.stocat.common.mysql.exception.ApiException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthCommandService {

    private final MemberRepository memberRepository;

    public Member create(@NonNull CreateMemberCommand cmd) {
        Member member = Member.create(
                cmd.nickname(),
                cmd.email(),
                cmd.encodedPassword(),
                cmd.provider(),
                cmd.providerId(),
                cmd.status(),
                cmd.role()
        );
        return memberRepository.save(member);
    }

    public void markLoginAt(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(AuthErrorCode.MEMBER_NOT_FOUND));
        member.markLoggedInNow();
    }

    // Update: status change
    public void changeStatus(Long memberId, MemberStatus status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(AuthErrorCode.MEMBER_NOT_FOUND));
        member.changeStatus(status);
        memberRepository.save(member);
    }

    // Delete
    public void delete(Long memberId) {
        memberRepository.deleteById(memberId);
    }
}
