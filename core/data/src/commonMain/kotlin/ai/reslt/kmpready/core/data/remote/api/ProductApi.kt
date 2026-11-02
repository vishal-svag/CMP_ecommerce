package ai.reslt.kmpready.core.data.remote.api

import ai.reslt.kmpready.core.data.remote.dto.ProductsResponseDto
import ai.reslt.kmpready.core.network.ApiEndpoints
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

internal class ProductApi(private val client: HttpClient) {

    suspend fun products(skip: Int, limit: Int): ProductsResponseDto =
        client.get(ApiEndpoints.PRODUCTS) {
            parameter("skip", skip)
            parameter("limit", limit)
            // Ask only for fields we render: smaller payloads, faster lists.
            parameter("select", PRODUCT_FIELDS)
        }.body()

    private companion object {
        const val PRODUCT_FIELDS =
            "title,description,category,price,discountPercentage,rating,stock,brand,thumbnail,images"
    }
}
