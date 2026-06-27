package com.omnixys.context;

import java.util.List;

public record PrincipalContext(
        String subject,
        String actorId,
        String userId,
        String tenantId,
        List<String> roles,
        String sessionId,
        String authStrength,
        Long authenticatedAtEpochMs
) {
    public PrincipalContext {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        if (roles == null) {
            roles = List.of();
        }
    }
}
