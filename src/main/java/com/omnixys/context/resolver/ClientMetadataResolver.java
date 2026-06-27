package com.omnixys.context.resolver;

import com.omnixys.context.ClientMetadata;
import jakarta.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface ClientMetadataResolver {
    ClientMetadata resolve(HttpServletRequest request);
}
