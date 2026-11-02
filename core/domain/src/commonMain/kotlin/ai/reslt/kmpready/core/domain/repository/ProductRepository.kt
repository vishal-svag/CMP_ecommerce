package ai.reslt.kmpready.core.domain.repository

import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.domain.model.Page
import ai.reslt.kmpready.core.domain.model.PageRequest
import ai.reslt.kmpready.core.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(request: PageRequest): AppResult<Page<Product>>
}
