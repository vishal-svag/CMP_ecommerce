package ai.reslt.kmpready.core.data

import ai.reslt.kmpready.core.data.mapper.toDomain
import ai.reslt.kmpready.core.data.remote.dto.ProductDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductMapperTest {
    @Test
    fun mapsPriceToMinorUnitsAndClampsBadData() {
        val product = ProductDto(
            id = 1, title = "Mascara", price = 9.99,
            discountPercentage = 150.0, rating = 7.0, stock = -3,
        ).toDomain()

        assertEquals(999L, product.price.minorUnits)
        assertEquals(100.0, product.discountPercent)
        assertEquals(5.0, product.rating)
        assertTrue(!product.inStock)
    }
}
