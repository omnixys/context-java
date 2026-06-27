package com.omnixys.context;

public record TransportMetadata(
        String type,
        String method,
        String route,
        String operation,
        String protocol,
        String host,
        String topic,
        Integer partition,
        String offset,
        String consumerGroup,
        String jobId,
        String jobType,
        Integer attempt
) {}
