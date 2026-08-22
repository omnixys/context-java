package com.omnixys.context;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrincipalContextTest {

    @Test
    void shouldExposeRecordAccessors() {
        var principal = new PrincipalContext(
                "usr-1", "act-1", "usr-1", "tenant-a",
                List.of("admin", "user"), "sess-1", "mfa", 1_700_000_000_000L);
        assertEquals("usr-1", principal.subject());
        assertEquals("act-1", principal.actorId());
        assertEquals("usr-1", principal.userId());
        assertEquals("tenant-a", principal.tenantId());
        assertEquals(List.of("admin", "user"), principal.roles());
        assertEquals("sess-1", principal.sessionId());
        assertEquals("mfa", principal.authStrength());
        assertEquals(1_700_000_000_000L, principal.authenticatedAtEpochMs());
    }

    @Test
    void shouldRejectNullSubject() {
        assertThrows(IllegalArgumentException.class, () -> new PrincipalContext(null, "a", "u", "t", List.of(), "s", "mfa", 0L));
    }

    @Test
    void shouldRejectBlankSubject() {
        assertThrows(IllegalArgumentException.class, () -> new PrincipalContext(" ", "a", "u", "t", List.of(), "s", "mfa", 0L));
    }

    @Test
    void shouldDefaultNullRolesToEmptyList() {
        var principal = new PrincipalContext("usr-1", "act-1", "usr-1", "tenant-a", null, "sess-1", "mfa", 0L);
        assertTrue(principal.roles().isEmpty());
    }
}
