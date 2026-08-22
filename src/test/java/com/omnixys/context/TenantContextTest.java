package com.omnixys.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantContextTest {

    @Test
    void shouldExposeRecordAccessors() {
        var context = new TenantContext("tenant-a", "trusted-header", true);
        assertEquals("tenant-a", context.tenantId());
        assertEquals("trusted-header", context.source());
        assertTrue(context.verified());
    }

    @Test
    void shouldRejectNullTenantId() {
        assertThrows(IllegalArgumentException.class, () -> new TenantContext(null, "h", true));
    }

    @Test
    void shouldRejectBlankTenantId() {
        assertThrows(IllegalArgumentException.class, () -> new TenantContext("   ", "h", true));
    }

    @Test
    void shouldDefaultNullSourceToUnknown() {
        var context = new TenantContext("tenant-b", null, false);
        assertEquals("unknown", context.source());
        assertFalse(context.verified());
    }
}
