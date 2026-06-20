package models

enum class UserRole(val displayName: String) {
    ADMIN("Администратор"),
    LOGOPED("Логопед"),
    STUDENT("Ученик")
}

data class User(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val phone: String?,
    val specialization: String?,
    val isBlocked: Boolean,
    val createdAt: Long
) {
    val fullName: String
        get() = buildString {
            append(lastName)
            append(" ")
            append(firstName)
            if (!middleName.isNullOrBlank()) {
                append(" ")
                append(middleName)
            }
        }
}
