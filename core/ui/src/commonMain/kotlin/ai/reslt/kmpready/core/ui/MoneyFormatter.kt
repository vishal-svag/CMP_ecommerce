package ai.reslt.kmpready.core.ui

import ai.reslt.kmpready.core.domain.model.Money
import kotlin.math.abs

/**
 * Locale-light money formatting that behaves identically on Android and iOS.
 * Swap for a platform NumberFormatter (expect/actual) when you add multi-locale support.
 */
object MoneyFormatter {
    private val symbols = mapOf("USD" to "$", "EUR" to "€", "GBP" to "£", "INR" to "₹", "JPY" to "¥")

    fun format(money: Money): String {
        val symbol = symbols[money.currencyCode] ?: "${money.currencyCode} "
        val sign = if (money.minorUnits < 0) "-" else ""
        val absolute = abs(money.minorUnits)
        val major = groupThousands((absolute / 100).toString())
        val minor = (absolute % 100).toString().padStart(2, '0')
        return "$sign$symbol$major.$minor"
    }

    private fun groupThousands(digits: String): String =
        digits.reversed().chunked(3).joinToString(",").reversed()
}
