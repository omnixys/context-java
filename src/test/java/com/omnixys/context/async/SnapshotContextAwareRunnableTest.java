package com.omnixys.context.async;

import com.omnixys.context.ContextAccessor;
import com.omnixys.context.ContextSnapshot;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.omnixys.context.ContextFixtures.clientMetadata;
import static com.omnixys.context.ContextFixtures.httpTransport;
import static com.omnixys.context.ContextFixtures.snapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotContextAwareRunnableTest {

    @Test
    void shouldRunDelegateWithContextCapturedAtConstruction() {
        ContextAccessor.set(snapshot());
        var task = new SnapshotContextAwareRunnable(() -> assertEquals(snapshot(), ContextAccessor.get()));
        ContextAccessor.clear();

        task.run();
        assertNull(ContextAccessor.get());
    }

    @Test
    void shouldRestorePreviousContextAfterRun() {
        ContextAccessor.set(snapshot());
        var task = new SnapshotContextAwareRunnable(() -> {});

        ContextSnapshot other = new ContextSnapshot(
                "req-2", "corr-2", 0L, null, null, clientMetadata(), httpTransport(), null);
        ContextAccessor.set(other);

        task.run();
        assertEquals(other, ContextAccessor.get());
        ContextAccessor.clear();
    }

    @Test
    void shouldClearContextWhenNoPreviousContext() {
        ContextAccessor.clear();
        var task = new SnapshotContextAwareRunnable(() -> {
            var other = new ContextSnapshot(
                    "req-2", "corr-2", 0L, null, null, clientMetadata(), httpTransport(), null);
            ContextAccessor.set(other);
        });

        task.run();
        assertNull(ContextAccessor.get());
    }

    @Test
    void shouldExecuteDelegateBody() {
        AtomicBoolean executed = new AtomicBoolean(false);
        ContextAccessor.set(snapshot());
        var task = new SnapshotContextAwareRunnable(() -> executed.set(true));
        ContextAccessor.clear();

        task.run();
        assertTrue(executed.get());
    }
}
