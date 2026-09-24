package org.sparcs.soap.networkingTests

import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.sparcs.soap.app.networking.otlDebugAuth

class OtlDebugAuthTest {
    private fun request(debug: Boolean, token: String) = Request.Builder().url("https://example.test/api/v2/users/info")
        .header("Authorization", "Bearer user-token").otlDebugAuth(debug, token).build()

    @Test fun `debug token supplements bearer authentication`() {
        val request = request(true, "debug-token")
        assertEquals("debug-token", request.header("X-SID-AUTH-TOKEN"))
        assertEquals("Bearer user-token", request.header("Authorization"))
    }

    @Test fun `blank tokens and release builds omit debug header`() {
        assertNull(request(true, "").header("X-SID-AUTH-TOKEN"))
        assertNull(request(true, "  ").header("X-SID-AUTH-TOKEN"))
        assertNull(request(false, "debug-token").header("X-SID-AUTH-TOKEN"))
    }
}
