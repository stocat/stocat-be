package com.stocat.authapi.service.dto;

import com.stocat.common.mysql.domain.member.domain.AuthProvider;
import com.stocat.common.mysql.domain.member.domain.MemberRole;
import com.stocat.common.mysql.domain.member.domain.MemberStatus;

public record CreateMemberCommand(
        String nickname,
        String email,
        String encodedPassword,
        AuthProvider provider,
        String providerId,
        MemberStatus status,
        MemberRole role
) {
}

