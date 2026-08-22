package com.omnixys.context.async;

import com.omnixys.context.ContextAccessor;
import com.omnixys.context.ContextSnapshot;
import org.junit.jupiter.api.Test;

import static com.omnixys.context.ContextFixtures.clientMetadata;
import static com.omnixys.context.ContextFixtures.httpTransport;
import static com.omnixys.context.ContextFixtures.snapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SnapshotContextAwareSupplierTest {

    @Test
    void shouldReturnValueAndClearContextWhenNoPreviousContext() {
        ContextAccessor.set(snapshot());
        var task = new SnapshotContextAwareSupplier<>(() -> "hello");
        ContextAccessor.clear();

        assertEquals("hello", task.get());
        assertNull(ContextAccessor.get());
    }

    @Test
    void shouldRestorePreviousContextAfterGet() {
        ContextAccessor.set(snapshot());
        var task = new SnapshotContextAwareSupplier<>(() -> "hello");

        ContextSnapshot other = new ContextSnapshot(
                "req-2", "corr-2", 0L, null, null, clientMetadata(), httpTransport(), null);
        ContextAccessor.set(other);

        assertEquals("hello", task.get());
        assertEquals(other, ContextAccessor.get());
        ContextAccessor.clear();
    }

    @Test
    void shouldPropagateExceptionAndRestoreContext() {
        ContextAccessor.set(snapshot());
        var task = new SnapshotContextAwareSupplier<>(() -> {
            throw new IllegalStateException("boom");
        });
        ContextAccessor.clear();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, task::get);
        assertNull(ContextAccessor.get());
    }
}
