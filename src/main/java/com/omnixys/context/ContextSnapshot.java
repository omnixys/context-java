package com.omnixys.context;

public record ContextSnapshot(
        String requestId,
        String correlationId,
        long startedAtEpochMs,
        TenantContext tenant,
        PrincipalContext principal,
        ClientMetadata client,
        TransportMetadata transport,
        TraceMetadata trace
) {
    public ContextSnapshot {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("correlationId must not be blank");
        }
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        }
        if (transport == null) {
            throw new IllegalArgumentException("transport must not be null");
        }
    }
}
