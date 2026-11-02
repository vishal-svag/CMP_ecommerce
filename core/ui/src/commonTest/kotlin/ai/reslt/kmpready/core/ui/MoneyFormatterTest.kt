package ai.reslt.kmpready.core.ui

import ai.reslt.kmpready.core.domain.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatterTest {
    @Test fun formatsCents() = assertEquals("$9.05", MoneyFormatter.format(Money(905, "USD")))
    @Test fun groupsThousands() = assertEquals("$1,234,567.89", MoneyFormatter.format(Money(123456789, "USD")))
    @Test fun unknownCurrencyFallsBackToCode() = assertEquals("AED 1.00", MoneyFormatter.format(Money(100, "AED")))
    @Test fun negative() = assertEquals("-$0.50", MoneyFormatter.format(Money(-50, "USD")))
}
