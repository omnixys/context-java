package com.omnixys.context.resolver;

import com.omnixys.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface TenantResolver {
    TenantContext resolve(HttpServletRequest request);
}
