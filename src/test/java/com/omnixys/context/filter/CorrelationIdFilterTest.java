package com.omnixys.context.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    jakarta.servlet.FilterChain chain;

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void shouldPropagateCorrelationIdAndSetResponseHeader() throws Exception {
        when(request.getHeader("X-Correlation-Id")).thenReturn("corr-1");
        when(request.getHeader("X-Request-Id")).thenReturn("req-1");
        when(response.containsHeader("X-Correlation-Id")).thenReturn(false);

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("corr-1", MDC.get("correlationId"));
            assertEquals("req-1", MDC.get("requestId"));
        });

        verify(response).setHeader("X-Correlation-Id", "corr-1");
        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("requestId"));
    }

    @Test
    void shouldUseRequestIdAsCorrelationIdWhenCorrelationMissing() throws Exception {
        when(request.getHeader("X-Correlation-Id")).thenReturn(null);
        when(request.getHeader("X-Request-Id")).thenReturn("req-42");
        when(response.containsHeader("X-Correlation-Id")).thenReturn(false);

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("req-42", MDC.get("correlationId"));
            assertEquals("req-42", MDC.get("requestId"));
        });

        verify(response).setHeader("X-Correlation-Id", "req-42");
    }

    @Test
    void shouldGenerateCorrelationIdWhenBothHeadersMissing() throws Exception {
        when(request.getHeader("X-Correlation-Id")).thenReturn(null);
        when(request.getHeader("X-Request-Id")).thenReturn(null);
        when(response.containsHeader("X-Correlation-Id")).thenReturn(false);

        filter.doFilter(request, response, (req, res) -> {
            String correlationId = MDC.get("correlationId");
            assertNotNull(correlationId);
            assertFalse(correlationId.isBlank());
        });

        verify(response).setHeader(anyString(), anyString());
        assertNull(MDC.get("correlationId"));
    }

    @Test
    void shouldNotOverwriteExistingResponseHeader() throws Exception {
        when(request.getHeader("X-Correlation-Id")).thenReturn("corr-1");
        when(response.containsHeader("X-Correlation-Id")).thenReturn(true);

        filter.doFilter(request, response, (req, res) -> {});
        verify(response, never()).setHeader(anyString(), anyString());
    }

    @Test
    void shouldRemoveMdcWhenChainThrows() throws Exception {
        when(request.getHeader("X-Correlation-Id")).thenReturn("corr-1");
        doThrow(new RuntimeException("boom")).when(chain).doFilter(request, response);

        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, chain));
        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("requestId"));
    }
}
