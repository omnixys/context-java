package com.omnixys.context.async;

import com.omnixys.context.ContextAccessor;
import com.omnixys.context.ContextSnapshot;

public class SnapshotContextAwareRunnable implements Runnable {

    private final Runnable delegate;
    private final ContextSnapshot snapshot;

    public SnapshotContextAwareRunnable(Runnable delegate) {
        this.delegate = delegate;
        this.snapshot = ContextAccessor.get();
    }

    @Override
    public void run() {
        ContextSnapshot previous = ContextAccessor.get();
        ContextAccessor.set(snapshot);
        try {
            delegate.run();
        } finally {
            if (previous != null) {
                ContextAccessor.set(previous);
            } else {
                ContextAccessor.clear();
            }
        }
    }
}
