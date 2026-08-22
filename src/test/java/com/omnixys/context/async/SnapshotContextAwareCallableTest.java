package com.omnixys.context.async;

import com.omnixys.context.ContextAccessor;
import com.omnixys.context.ContextSnapshot;
import org.junit.jupiter.api.Test;

import static com.omnixys.context.ContextFixtures.clientMetadata;
import static com.omnixys.context.ContextFixtures.httpTransport;
import static com.omnixys.context.ContextFixtures.snapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SnapshotContextAwareCallableTest {

    @Test
    void shouldReturnValueAndClearContextWhenNoPreviousContext() throws Exception {
        ContextAccessor.set(snapshot());
        var task = new SnapshotContextAwareCallable<>(() -> "42");
        ContextAccessor.clear();

        assertEquals("42", task.call());
        assertNull(ContextAccessor.get());
    }

    @Test
    void shouldRestorePreviousContextAfterCall() throws Exception {
        ContextAccessor.set(snapshot());
        var task = new SnapshotContextAwareCallable<>(() -> "42");

        ContextSnapshot other = new ContextSnapshot(
                "req-2", "corr-2", 0L, null, null, clientMetadata(), httpTransport(), null);
        ContextAccessor.set(other);

        assertEquals("42", task.call());
        assertEquals(other, ContextAccessor.get());
        ContextAccessor.clear();
    }

    @Test
    void shouldPropagateExceptionAndRestoreContext() {
        ContextAccessor.set(snapshot());
        var task = new SnapshotContextAwareCallable<>(() -> {
            throw new IllegalStateException("boom");
        });
        ContextAccessor.clear();

        assertThrows(IllegalStateException.class, task::call);
        assertNull(ContextAccessor.get());
    }
}
