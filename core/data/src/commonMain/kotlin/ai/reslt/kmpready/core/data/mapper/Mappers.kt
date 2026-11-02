package ai.reslt.kmpready.core.data.mapper

import ai.reslt.kmpready.core.data.remote.dto.LoginResponseDto
import ai.reslt.kmpready.core.data.remote.dto.ProductDto
import ai.reslt.kmpready.core.data.remote.dto.UserDto
import ai.reslt.kmpready.core.domain.model.Money
import ai.reslt.kmpready.core.domain.model.Product
import ai.reslt.kmpready.core.domain.model.User

/** Backend sends USD as a major-unit double; normalise to minor units once, here. */
internal const val DEFAULT_CURRENCY = "USD"

internal fun UserDto.toDomain(): User = User(
    id = id,
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    avatarUrl = image,
)

internal fun LoginResponseDto.toUser(): User = User(
    id = id,
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    avatarUrl = image,
)

internal fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title,
    description = description,
    brand = brand,
    category = category,
    price = Money.fromMajor(price, DEFAULT_CURRENCY),
    discountPercent = discountPercentage.coerceIn(0.0, 100.0),
    rating = rating.coerceIn(0.0, 5.0),
    stock = stock.coerceAtLeast(0),
    thumbnailUrl = thumbnail,
    imageUrls = images,
)
