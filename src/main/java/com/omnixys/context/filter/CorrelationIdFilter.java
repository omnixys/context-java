package com.omnixys.context.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.util.UUID;

@Order(0)
public class CorrelationIdFilter implements Filter {

    static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    static final String REQUEST_ID_HEADER = "X-Request-Id";
    static final String MDC_CORRELATION_ID = "correlationId";
    static final String MDC_REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            if (request instanceof HttpServletRequest httpRequest) {
                String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
                if (correlationId == null || correlationId.isBlank()) {
                    String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
                    correlationId = (requestId != null && !requestId.isBlank()) ? requestId : UUID.randomUUID().toString();
                }
                MDC.put(MDC_CORRELATION_ID, correlationId);

                String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
                if (requestId != null && !requestId.isBlank()) {
                    MDC.put(MDC_REQUEST_ID, requestId);
                }

                if (response instanceof HttpServletResponse httpResponse) {
                    if (!httpResponse.containsHeader(CORRELATION_ID_HEADER)) {
                        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
                    }
                }
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_REQUEST_ID);
        }
    }
}
