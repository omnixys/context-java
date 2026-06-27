package com.omnixys.context.async;

import com.omnixys.context.ContextAccessor;
import com.omnixys.context.ContextSnapshot;

import java.util.concurrent.Callable;

public class SnapshotContextAwareCallable<V> implements Callable<V> {

    private final Callable<V> delegate;
    private final ContextSnapshot snapshot;

    public SnapshotContextAwareCallable(Callable<V> delegate) {
        this.delegate = delegate;
        this.snapshot = ContextAccessor.get();
    }

    @Override
    public V call() throws Exception {
        ContextSnapshot previous = ContextAccessor.get();
        ContextAccessor.set(snapshot);
        try {
            return delegate.call();
        } finally {
            if (previous != null) {
                ContextAccessor.set(previous);
            } else {
                ContextAccessor.clear();
            }
        }
    }
}
