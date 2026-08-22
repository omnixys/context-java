package com.omnixys.context.resolver;

import com.omnixys.context.ContextAccessor;
import com.omnixys.context.ContextSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static com.omnixys.context.ContextFixtures.snapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextArgumentResolverTest {

    private final ContextArgumentResolver resolver = new ContextArgumentResolver();

    @SuppressWarnings("unused")
    private static class SampleController {
        void handle(ContextSnapshot snapshot) {}

        void handle(String name) {}
    }

    private static MethodParameter parameter(Class<?> type) throws NoSuchMethodException {
        Method method = SampleController.class.getDeclaredMethod("handle", type);
        return new MethodParameter(method, 0);
    }

    @Test
    void shouldSupportContextSnapshotParameter() throws NoSuchMethodException {
        assertTrue(resolver.supportsParameter(parameter(ContextSnapshot.class)));
    }

    @Test
    void shouldNotSupportNonContextParameter() throws NoSuchMethodException {
        assertFalse(resolver.supportsParameter(parameter(String.class)));
    }

    @Test
    void shouldResolveArgumentFromContextAccessor() throws NoSuchMethodException {
        ContextAccessor.set(snapshot());
        try {
            assertEquals(snapshot(), resolver.resolveArgument(parameter(ContextSnapshot.class), null, null, null));
        } finally {
            ContextAccessor.clear();
        }
    }

    @Test
    void shouldReturnNullWhenNoContextPresent() throws NoSuchMethodException {
        ContextAccessor.clear();
        assertNull(resolver.resolveArgument(parameter(ContextSnapshot.class), null, null, null));
    }
}
