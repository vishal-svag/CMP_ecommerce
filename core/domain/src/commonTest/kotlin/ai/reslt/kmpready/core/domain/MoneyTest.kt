package ai.reslt.kmpready.core.domain

import ai.reslt.kmpready.core.domain.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyTest {
    @Test
    fun fromMajor_avoidsFloatingPointDrift() {
        assertEquals(1999L, Money.fromMajor(19.99, "USD").minorUnits)
    }

    @Test
    fun discountedBy_roundsToNearestCent() {
        val price = Money(999, "USD") // $9.99
        assertEquals(849L, price.discountedBy(15.0).minorUnits) // 849.15 -> 849
    }

    @Test
    fun discountedBy_rejectsInvalidPercent() {
        assertFailsWith<IllegalArgumentException> { Money(100, "USD").discountedBy(120.0) }
    }

    @Test
    fun compare_differentCurrencies_throws() {
        assertFailsWith<IllegalArgumentException> { Money(1, "USD") < Money(1, "EUR") }
    }
}
