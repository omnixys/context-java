package com.omnixys.context.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeaderTenantResolverTest {

    @Mock
    HttpServletRequest request;

    private final HeaderTenantResolver resolver = new HeaderTenantResolver();

    @Test
    void shouldResolveTenantFromHeader() {
        when(request.getHeader("X-Tenant-Id")).thenReturn("tenant-a");
        var tenant = resolver.resolve(request);
        assertEquals("tenant-a", tenant.tenantId());
        assertEquals("trusted-header", tenant.source());
        assertTrue(tenant.verified());
    }

    @Test
    void shouldReturnNullWhenHeaderMissing() {
        when(request.getHeader("X-Tenant-Id")).thenReturn(null);
        assertNull(resolver.resolve(request));
    }

    @Test
    void shouldReturnNullWhenHeaderBlank() {
        when(request.getHeader("X-Tenant-Id")).thenReturn("   ");
        assertNull(resolver.resolve(request));
    }
}
