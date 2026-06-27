package com.omnixys.context;

public record ClientMetadata(
        String ip,
        String userAgent,
        String deviceId,
        String locale,
        String timezone,
        ClientSoftware browser,
        ClientSoftware os,
        DeviceInfo device,
        LocationInfo location
) {
    public record ClientSoftware(
            String name,
            String version
    ) {}

    public record DeviceInfo(
            String type,
            String vendor,
            String model
    ) {}

    public record LocationInfo(
            String country,
            String region,
            String city,
            String source
    ) {}
}
