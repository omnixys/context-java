package com.omnixys.context.resolver;

import com.omnixys.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;

public class HeaderTenantResolver implements TenantResolver {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    public TenantContext resolve(HttpServletRequest request) {
        String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId == null || tenantId.isBlank()) {
            return null;
        }
        return new TenantContext(tenantId, "trusted-header", true);
    }
}
