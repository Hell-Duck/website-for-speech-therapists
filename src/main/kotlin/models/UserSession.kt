package models

data class UserSession(
    val userId: Int,
    val email: String,
    val role: String,
    val firstName: String
)
