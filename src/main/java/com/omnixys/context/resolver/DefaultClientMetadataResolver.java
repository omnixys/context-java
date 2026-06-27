package com.omnixys.context.resolver;

import com.omnixys.context.ClientMetadata;
import jakarta.servlet.http.HttpServletRequest;

public class DefaultClientMetadataResolver implements ClientMetadataResolver {

    @Override
    public ClientMetadata resolve(HttpServletRequest request) {
        return new ClientMetadata(
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("X-Device-Id"),
                request.getHeader("Accept-Language"),
                null, null, null, null, null
        );
    }
}
