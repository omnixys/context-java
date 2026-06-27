package com.omnixys.context;

public record TraceMetadata(
        String traceId,
        String spanId
) {}
