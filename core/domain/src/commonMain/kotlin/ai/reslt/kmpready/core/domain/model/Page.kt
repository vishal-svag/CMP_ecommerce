package ai.reslt.kmpready.core.domain.model

data class PageRequest(
    val offset: Int,
    val limit: Int = DEFAULT_LIMIT,
) {
    init {
        require(offset >= 0) { "offset must be >= 0" }
        require(limit in 1..MAX_LIMIT) { "limit must be within 1..$MAX_LIMIT" }
    }

    companion object {
        const val DEFAULT_LIMIT = 20
        const val MAX_LIMIT = 100
    }
}

data class Page<T>(
    val items: List<T>,
    val offset: Int,
    val total: Int,
) {
    val hasMore: Boolean get() = offset + items.size < total
    val nextOffset: Int get() = offset + items.size
}
