package com.omnixys.context.resolver;

import com.omnixys.context.PrincipalContext;
import jakarta.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface PrincipalResolver {
    PrincipalContext resolve(HttpServletRequest request);
}
