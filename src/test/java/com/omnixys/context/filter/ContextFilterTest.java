package com.omnixys.context.filter;

import com.omnixys.context.ContextAccessor;
import com.omnixys.context.PrincipalContext;
import com.omnixys.context.resolver.PrincipalResolver;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextFilterTest {

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    jakarta.servlet.FilterChain chain;
    @Mock
    ServletRequest plainRequest;

    private void stubHttpRequest() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getServerName()).thenReturn("localhost");
    }

    @Test
    void shouldBuildSnapshotFromRequestHeaders() throws Exception {
        when(request.getHeader("X-Request-Id")).thenReturn("req-1");
        when(request.getHeader("X-Correlation-Id")).thenReturn("corr-1");
        when(request.getHeader("X-Tenant-Id")).thenReturn("tenant-a");
        when(request.getHeader("X-Trace-Id")).thenReturn("trace-1");
        when(request.getHeader("X-Span-Id")).thenReturn("span-1");
        when(request.getHeader("User-Agent")).thenReturn("curl/8");
        when(request.getHeader("X-Device-Id")).thenReturn("dev-9");
        when(request.getHeader("Accept-Language")).thenReturn("fr-FR");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getProtocol()).thenReturn("HTTP/2");
        when(request.getServerName()).thenReturn("api.example.com");

        var filter = new ContextFilter();
        filter.doFilter(request, response, (req, res) -> {
            var ctx = ContextAccessor.get();
            assertNotNull(ctx);
            assertEquals("req-1", ctx.requestId());
            assertEquals("corr-1", ctx.correlationId());
            assertEquals("tenant-a", ctx.tenant().tenantId());
            assertEquals("trusted-header", ctx.tenant().source());
            assertEquals("trace-1", ctx.trace().traceId());
            assertEquals("span-1", ctx.trace().spanId());
            assertEquals("10.0.0.1", ctx.client().ip());
            assertEquals("curl/8", ctx.client().userAgent());
            assertEquals("dev-9", ctx.client().deviceId());
            assertEquals("fr-FR", ctx.client().locale());
            assertEquals("http", ctx.transport().type());
            assertEquals("POST", ctx.transport().method());
            assertEquals("/api/orders", ctx.transport().route());
            assertEquals("HTTP/2", ctx.transport().protocol());
            assertEquals("api.example.com", ctx.transport().host());
        });

        assertNull(ContextAccessor.get());
    }

    @Test
    void shouldGenerateIdsAndDefaultCorrelationToRequestId() throws Exception {
        stubHttpRequest();
        var filter = new ContextFilter();
        filter.doFilter(request, response, (req, res) -> {
            var ctx = ContextAccessor.get();
            assertNotNull(ctx.requestId());
            assertNotNull(ctx.correlationId());
            assertEquals(ctx.requestId(), ctx.correlationId());
            assertNull(ctx.tenant());
            assertNull(ctx.principal());
            assertNull(ctx.trace());
        });
        assertNull(ContextAccessor.get());
    }

    @Test
    void shouldEnrichAndClearMdc() throws Exception {
        when(request.getHeader("X-Request-Id")).thenReturn("req-1");
        when(request.getHeader("X-Correlation-Id")).thenReturn("corr-1");
        when(request.getHeader("X-Tenant-Id")).thenReturn("tenant-a");
        stubHttpRequest();

        var filter = new ContextFilter();
        filter.doFilter(request, response, (req, res) -> {
            assertEquals("corr-1", MDC.get("correlationId"));
            assertEquals("req-1", MDC.get("requestId"));
            assertEquals("tenant-a", MDC.get("tenantId"));
        });

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("requestId"));
        assertNull(MDC.get("tenantId"));
        assertNull(MDC.get("actorId"));
        assertNull(MDC.get("traceId"));
    }

    @Test
    void shouldResolvePrincipalAndCaptureActorInMdc() throws Exception {
        PrincipalResolver resolver = req -> new PrincipalContext(
                "sub-1", "actor-9", "usr-9", "tenant-a", List.of("admin"), "sess-1", "mfa", 0L);
        stubHttpRequest();

        var filter = new ContextFilter(resolver);
        filter.doFilter(request, response, (req, res) -> {
            var ctx = ContextAccessor.get();
            assertNotNull(ctx.principal());
            assertEquals("actor-9", ctx.principal().actorId());
            assertEquals("usr-9", ctx.principal().userId());
            assertEquals(List.of("admin"), ctx.principal().roles());
            assertEquals("actor-9", MDC.get("actorId"));
        });

        assertNull(ContextAccessor.get());
        assertNull(MDC.get("actorId"));
    }

    @Test
    void shouldContinueWhenPrincipalResolverThrows() throws Exception {
        PrincipalResolver resolver = req -> {
            throw new RuntimeException("auth down");
        };
        stubHttpRequest();

        var filter = new ContextFilter(resolver);
        filter.doFilter(request, response, (req, res) -> {
            assertNotNull(ContextAccessor.get());
            assertNull(ContextAccessor.get().principal());
        });
        assertNull(ContextAccessor.get());
    }

    @Test
    void shouldClearContextAndMdcWhenChainThrows() throws Exception {
        when(request.getHeader("X-Request-Id")).thenReturn("req-1");
        stubHttpRequest();
        doThrow(new java.io.IOException("boom")).when(chain).doFilter(request, response);

        var filter = new ContextFilter();
        assertThrows(java.io.IOException.class, () -> filter.doFilter(request, response, chain));
        assertNull(ContextAccessor.get());
        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("requestId"));
    }

    @Test
    void shouldPassThroughNonHttpRequestsWithoutSettingContext() throws Exception {
        var filter = new ContextFilter();
        filter.doFilter(plainRequest, response, chain);
        verify(chain).doFilter(plainRequest, response);
        assertNull(ContextAccessor.get());
    }
}
