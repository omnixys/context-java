package com.omnixys.context;

public final class ContextAccessor {

    private static final ThreadLocal<ContextSnapshot> CONTEXT_HOLDER = new ThreadLocal<>();

    private ContextAccessor() {}

    public static void set(ContextSnapshot snapshot) {
        CONTEXT_HOLDER.set(snapshot);
    }

    public static ContextSnapshot get() {
        return CONTEXT_HOLDER.get();
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    public static boolean isPresent() {
        return CONTEXT_HOLDER.get() != null;
    }
}
