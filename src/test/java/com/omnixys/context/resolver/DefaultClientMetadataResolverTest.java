package com.omnixys.context.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultClientMetadataResolverTest {

    @Mock
    HttpServletRequest request;

    private final DefaultClientMetadataResolver resolver = new DefaultClientMetadataResolver();

    @Test
    void shouldMapRequestToClientMetadata() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("curl/8.4");
        when(request.getHeader("X-Device-Id")).thenReturn("dev-7");
        when(request.getHeader("Accept-Language")).thenReturn("en-US");

        var metadata = resolver.resolve(request);
        assertEquals("127.0.0.1", metadata.ip());
        assertEquals("curl/8.4", metadata.userAgent());
        assertEquals("dev-7", metadata.deviceId());
        assertEquals("en-US", metadata.locale());
    }

    @Test
    void shouldLeaveOptionalFieldsNullWhenHeadersMissing() {
        when(request.getRemoteAddr()).thenReturn("10.0.0.9");
        var metadata = resolver.resolve(request);
        assertEquals("10.0.0.9", metadata.ip());
        assertNull(metadata.userAgent());
        assertNull(metadata.deviceId());
        assertNull(metadata.locale());
        assertNull(metadata.timezone());
        assertNull(metadata.browser());
        assertNull(metadata.os());
        assertNull(metadata.device());
        assertNull(metadata.location());
    }
}
