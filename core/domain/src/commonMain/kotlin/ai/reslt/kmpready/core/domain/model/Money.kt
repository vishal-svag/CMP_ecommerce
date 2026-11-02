package ai.reslt.kmpready.core.domain.model

import kotlin.math.roundToLong

/**
 * Money in minor units (cents). Never use Double for prices — floating point
 * rounding errors compound in carts and totals.
 */
data class Money(
    val minorUnits: Long,
    val currencyCode: String,
) : Comparable<Money> {

    fun discountedBy(percent: Double): Money {
        require(percent in 0.0..100.0) { "percent must be within 0..100" }
        val discounted = (minorUnits * (100.0 - percent) / 100.0).roundToLong()
        return copy(minorUnits = discounted)
    }

    override fun compareTo(other: Money): Int {
        require(currencyCode == other.currencyCode) { "Cannot compare different currencies" }
        return minorUnits.compareTo(other.minorUnits)
    }

    companion object {
        fun fromMajor(amount: Double, currencyCode: String): Money =
            Money((amount * 100).roundToLong(), currencyCode)
    }
}
