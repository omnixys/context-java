package com.omnixys.context;

public record TenantContext(
        String tenantId,
        String source,
        boolean verified
) {
    public TenantContext {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be blank");
        }
        if (source == null) {
            source = "unknown";
        }
    }
}
