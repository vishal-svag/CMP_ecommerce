package ai.reslt.kmpready.core.domain.usecase

import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.domain.model.Page
import ai.reslt.kmpready.core.domain.model.PageRequest
import ai.reslt.kmpready.core.domain.model.Product
import ai.reslt.kmpready.core.domain.repository.ProductRepository

class GetProductsUseCase(
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke(offset: Int, limit: Int = PageRequest.DEFAULT_LIMIT): AppResult<Page<Product>> =
        productRepository.getProducts(PageRequest(offset, limit))
}
