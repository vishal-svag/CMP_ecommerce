package ai.reslt.kmpready.core.data.repository

import ai.reslt.kmpready.core.common.coroutines.DispatcherProvider
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.data.mapper.toDomain
import ai.reslt.kmpready.core.data.remote.api.ProductApi
import ai.reslt.kmpready.core.domain.model.Page
import ai.reslt.kmpready.core.domain.model.PageRequest
import ai.reslt.kmpready.core.domain.model.Product
import ai.reslt.kmpready.core.domain.repository.ProductRepository
import ai.reslt.kmpready.core.network.safeApiCall
import kotlinx.coroutines.withContext

internal class ProductRepositoryImpl(
    private val api: ProductApi,
    private val dispatchers: DispatcherProvider,
) : ProductRepository {

    override suspend fun getProducts(request: PageRequest): AppResult<Page<Product>> =
        withContext(dispatchers.io) {
            safeApiCall {
                val response = api.products(skip = request.offset, limit = request.limit)
                Page(
                    items = response.products.map { it.toDomain() },
                    offset = response.skip,
                    total = response.total,
                )
            }
        }
}
