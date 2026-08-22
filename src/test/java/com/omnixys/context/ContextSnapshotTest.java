package com.omnixys.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContextSnapshotTest {

    private static final ClientMetadata CLIENT = new ClientMetadata(null, null, null, null, null, null, null, null, null);
    private static final TransportMetadata TRANSPORT = new TransportMetadata(null, null, null, null, null, null, null, null, null, null, null, null, null);

    @Test
    void shouldRejectNullRequestId() {
        assertThrows(IllegalArgumentException.class, () -> new ContextSnapshot(null, "corr", 0L, null, null, CLIENT, TRANSPORT, null));
    }

    @Test
    void shouldRejectBlankRequestId() {
        assertThrows(IllegalArgumentException.class, () -> new ContextSnapshot(" ", "corr", 0L, null, null, CLIENT, TRANSPORT, null));
    }

    @Test
    void shouldRejectBlankCorrelationId() {
        assertThrows(IllegalArgumentException.class, () -> new ContextSnapshot("req", "", 0L, null, null, CLIENT, TRANSPORT, null));
    }

    @Test
    void shouldRejectNullClient() {
        assertThrows(IllegalArgumentException.class, () -> new ContextSnapshot("req", "corr", 0L, null, null, null, TRANSPORT, null));
    }

    @Test
    void shouldRejectNullTransport() {
        assertThrows(IllegalArgumentException.class, () -> new ContextSnapshot("req", "corr", 0L, null, null, CLIENT, null, null));
    }

    @Test
    void shouldAllowOptionalFieldsNull() {
        var snapshot = new ContextSnapshot("req-1", "corr-1", 42L, null, null, CLIENT, TRANSPORT, null);
        assertEquals("req-1", snapshot.requestId());
        assertEquals("corr-1", snapshot.correlationId());
        assertEquals(42L, snapshot.startedAtEpochMs());
        assertNull(snapshot.tenant());
        assertNull(snapshot.principal());
        assertNull(snapshot.trace());
    }
}
