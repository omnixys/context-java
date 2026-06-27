package com.omnixys.context.filter;

import com.omnixys.context.*;
import com.omnixys.context.resolver.PrincipalResolver;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.util.UUID;

@Order(1)
public class ContextFilter implements Filter {

    private final PrincipalResolver principalResolver;

    public ContextFilter() {
        this(null);
    }

    public ContextFilter(PrincipalResolver principalResolver) {
        this.principalResolver = principalResolver;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            if (request instanceof HttpServletRequest httpRequest) {
                ContextSnapshot snapshot = buildSnapshot(httpRequest);
                ContextAccessor.set(snapshot);
                enrichMdc(snapshot);
            }
            chain.doFilter(request, response);
        } finally {
            clearMdc();
            ContextAccessor.clear();
        }
    }

    private ContextSnapshot buildSnapshot(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = requestId;
        }

        String tenantId = request.getHeader("X-Tenant-Id");
        TenantContext tenant = null;
        if (tenantId != null && !tenantId.isBlank()) {
            tenant = new TenantContext(tenantId, "trusted-header", true);
        }

        PrincipalContext principal = null;
        if (principalResolver != null) {
            try {
                principal = principalResolver.resolve(request);
            } catch (Exception e) {
                // Principal resolution failure is non-fatal
            }
        }

        ClientMetadata client = resolveClientMetadata(request);
        TransportMetadata transport = resolveTransportMetadata(request);

        TraceMetadata trace = null;
        String traceId = request.getHeader("X-Trace-Id");
        String spanId = request.getHeader("X-Span-Id");
        if (traceId != null && !traceId.isBlank()) {
            trace = new TraceMetadata(traceId, spanId);
        }

        return new ContextSnapshot(
                requestId, correlationId, System.currentTimeMillis(),
                tenant, principal, client, transport, trace
        );
    }

    private ClientMetadata resolveClientMetadata(HttpServletRequest request) {
        return new ClientMetadata(
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("X-Device-Id"),
                request.getHeader("Accept-Language"),
                null,
                null, null, null, null
        );
    }

    private TransportMetadata resolveTransportMetadata(HttpServletRequest request) {
        return new TransportMetadata(
                "http",
                request.getMethod(),
                request.getRequestURI(),
                null,
                request.getProtocol(),
                request.getServerName(),
                null, null, null, null,
                null, null, null
        );
    }

    private void enrichMdc(ContextSnapshot snapshot) {
        if (snapshot == null) return;
        MDC.put("correlationId", snapshot.correlationId());
        MDC.put("requestId", snapshot.requestId());
        if (snapshot.tenant() != null) {
            MDC.put("tenantId", snapshot.tenant().tenantId());
        }
        if (snapshot.principal() != null) {
            MDC.put("actorId", snapshot.principal().actorId());
        }
        if (snapshot.trace() != null && snapshot.trace().traceId() != null) {
            MDC.put("traceId", snapshot.trace().traceId());
        }
    }

    private void clearMdc() {
        MDC.remove("correlationId");
        MDC.remove("requestId");
        MDC.remove("tenantId");
        MDC.remove("actorId");
        MDC.remove("traceId");
    }
}
