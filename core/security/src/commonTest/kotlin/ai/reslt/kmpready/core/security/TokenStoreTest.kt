package ai.reslt.kmpready.core.security

import ai.reslt.kmpready.core.common.time.AppClock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TokenStoreTest {
    private var now = 1_000L
    private val clock = AppClock { now }
    private val storage = InMemorySecureStorage()

    private fun store() = TokenStore(storage, clock)

    @Test
    fun ephemeral_neverTouchesDisk_andDiesWithProcess() = runTest {
        store().start(AuthTokens("a", "r"), persistForMillis = null)
        assertTrue(storage.values.isEmpty())
        assertNull(store().read()) // "new process"
    }

    @Test
    fun persisted_survivesProcess_untilExpiry() = runTest {
        store().start(AuthTokens("a", "r"), persistForMillis = 10_000)
        assertEquals(AuthTokens("a", "r"), store().read())

        now += 10_000
        assertNull(store().read())
        assertTrue(storage.values.isEmpty()) // expired data is wiped
    }

    @Test
    fun update_keepsModeAndExpiry() = runTest {
        val s = store()
        s.start(AuthTokens("a", "r"), persistForMillis = 5_000)
        s.update(AuthTokens("a2", "r2"))
        assertEquals(AuthTokens("a2", "r2"), store().read())
        now += 5_000
        assertNull(store().read())
    }

    @Test
    fun partialTokens_areTreatedAsNoSession() = runTest {
        storage.put("auth.access_token", "a")
        assertNull(store().read())
    }

    @Test
    fun clear_removesEverything() = runTest {
        val s = store()
        s.start(AuthTokens("a", "r"), persistForMillis = 5_000)
        s.clear()
        assertNull(s.read())
        assertTrue(storage.values.isEmpty())
    }

    @Test
    fun toString_redactsTokens() {
        assertTrue("secret" !in AuthTokens("secret", "secret").toString())
    }
}
