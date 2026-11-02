package ai.reslt.kmpready.core.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ProductsResponseDto(
    val products: List<ProductDto> = emptyList(),
    val total: Int = 0,
    val skip: Int = 0,
    val limit: Int = 0,
)

@Serializable
internal data class ProductDto(
    val id: Long,
    val title: String,
    val description: String = "",
    val category: String = "",
    val price: Double,
    val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val brand: String? = null,
    val thumbnail: String = "",
    val images: List<String> = emptyList(),
)
