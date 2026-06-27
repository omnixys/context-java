package com.omnixys.context.async;

import com.omnixys.context.ContextAccessor;
import com.omnixys.context.ContextSnapshot;

import java.util.function.Supplier;

public class SnapshotContextAwareSupplier<T> implements Supplier<T> {

    private final Supplier<T> delegate;
    private final ContextSnapshot snapshot;

    public SnapshotContextAwareSupplier(Supplier<T> delegate) {
        this.delegate = delegate;
        this.snapshot = ContextAccessor.get();
    }

    @Override
    public T get() {
        ContextSnapshot previous = ContextAccessor.get();
        ContextAccessor.set(snapshot);
        try {
            return delegate.get();
        } finally {
            if (previous != null) {
                ContextAccessor.set(previous);
            } else {
                ContextAccessor.clear();
            }
        }
    }
}
