package com.omnixys.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContextAccessorTest {

    @Test
    void shouldSetAndGetContext() {
        var snapshot = new ContextSnapshot(
                "req-1", "corr-1", System.currentTimeMillis(),
                null, null, new ClientMetadata(null, null, null, null, null, null, null, null, null),
                new TransportMetadata("http", null, null, null, null, null, null, null, null, null, null, null, null),
                null
        );
        ContextAccessor.set(snapshot);
        assertEquals(snapshot, ContextAccessor.get());
        assertTrue(ContextAccessor.isPresent());
        ContextAccessor.clear();
        assertNull(ContextAccessor.get());
        assertFalse(ContextAccessor.isPresent());
    }
}
