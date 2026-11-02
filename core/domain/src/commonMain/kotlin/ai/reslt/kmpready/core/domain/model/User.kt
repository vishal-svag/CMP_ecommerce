package ai.reslt.kmpready.core.domain.model

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String?,
) {
    val displayName: String get() = firstName.ifBlank { username }
}
