package com.omnixys.context;

import com.omnixys.context.ClientMetadata.ClientSoftware;
import com.omnixys.context.ClientMetadata.DeviceInfo;
import com.omnixys.context.ClientMetadata.LocationInfo;

import java.util.List;

public final class ContextFixtures {

    private ContextFixtures() {}

    public static final long STARTED_AT = 1_700_000_000_000L;

    public static ClientMetadata clientMetadata() {
        return new ClientMetadata(
                "10.0.0.1", "Mozilla/5.0", "dev-1", "de-DE", "Europe/Berlin",
                new ClientSoftware("Chrome", "126.0"),
                new ClientSoftware("iOS", "17.5"),
                new DeviceInfo("mobile", "Apple", "iPhone 15"),
                new LocationInfo("DE", "Bayern", "München", "geoip")
        );
    }

    public static TransportMetadata httpTransport() {
        return new TransportMetadata(
                "http", "GET", "/api/v1/orders", "get-orders", "HTTP/1.1", "api.example.com",
                null, null, null, null, null, null, null
        );
    }

    public static ContextSnapshot snapshot() {
        return new ContextSnapshot(
                "req-1", "corr-1", STARTED_AT,
                new TenantContext("tenant-a", "trusted-header", true),
                new PrincipalContext(
                        "usr-1", "act-1", "usr-1", "tenant-a",
                        List.of("admin", "user"), "sess-1", "mfa", STARTED_AT),
                clientMetadata(), httpTransport(), new TraceMetadata("trace-1", "span-1")
        );
    }
}
