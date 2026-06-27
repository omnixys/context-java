package com.omnixys.context.async;

import com.omnixys.context.ContextAccessor;
import com.omnixys.context.ContextSnapshot;
import org.springframework.core.task.TaskDecorator;

public class ContextAwareTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        ContextSnapshot snapshot = ContextAccessor.get();
        return () -> {
            ContextSnapshot previous = ContextAccessor.get();
            ContextAccessor.set(snapshot);
            try {
                runnable.run();
            } finally {
                if (previous != null) {
                    ContextAccessor.set(previous);
                } else {
                    ContextAccessor.clear();
                }
            }
        };
    }
}
