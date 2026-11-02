package ai.reslt.kmpready.core.domain.model

data class Product(
    val id: Long,
    val title: String,
    val description: String,
    val brand: String?,
    val category: String,
    val price: Money,
    val discountPercent: Double,
    val rating: Double,
    val stock: Int,
    val thumbnailUrl: String,
    val imageUrls: List<String>,
) {
    val finalPrice: Money get() = if (discountPercent > 0) price.discountedBy(discountPercent) else price
    val hasDiscount: Boolean get() = finalPrice < price
    val inStock: Boolean get() = stock > 0
}
