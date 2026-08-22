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

class ContextAwareTaskDecoratorTest {

    private final ContextAwareTaskDecorator decorator = new ContextAwareTaskDecorator();

    @Test
    void shouldPropagateSnapshotIntoDecoratedTask() {
        ContextAccessor.set(snapshot());
        AtomicBoolean executed = new AtomicBoolean(false);
        Runnable task = decorator.decorate(() -> {
            assertEquals(snapshot(), ContextAccessor.get());
            executed.set(true);
        });
        ContextAccessor.clear();

        task.run();
        assertTrue(executed.get());
        assertNull(ContextAccessor.get());
    }

    @Test
    void shouldRestorePreviousContextAfterTask() {
        ContextAccessor.set(snapshot());
        Runnable task = decorator.decorate(() -> {});
        ContextAccessor.clear();

        ContextSnapshot other = new ContextSnapshot(
                "req-2", "corr-2", 0L, null, null, clientMetadata(), httpTransport(), null);
        ContextAccessor.set(other);

        task.run();
        assertEquals(other, ContextAccessor.get());
        ContextAccessor.clear();
    }

    @Test
    void shouldPropagateNullSnapshotWhenNoContextPresent() {
        ContextAccessor.clear();
        AtomicBoolean executed = new AtomicBoolean(false);
        Runnable task = decorator.decorate(() -> {
            assertNull(ContextAccessor.get());
            executed.set(true);
        });

        task.run();
        assertTrue(executed.get());
    }
}
