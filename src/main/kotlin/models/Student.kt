package models

enum class StudentStatus(val displayName: String) {
    ACTIVE("Активный"),
    COMPLETED("Завершён")
}

data class StudentCard(
    val id: Int,
    val studentUserId: Int,
    val logopedId: Int,
    val conclusion: String?,
    val correctionPlan: String?,
    val notes: String?,
    val status: StudentStatus,
    val createdAt: Long
)

data class StudentCardView(
    val card: StudentCard,
    val studentUser: User
)

/** Запись из истории работы с учеником (предыдущие карточки других / текущего логопеда) */
data class CardHistoryEntry(
    val card: StudentCard,
    val logopedName: String
)
