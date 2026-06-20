package models

import java.time.LocalDate

data class Session(
    val id: Int,
    val studentUserId: Int,
    val logopedId: Int,
    val sessionDate: String,   // yyyy-MM-dd
    val exercises: String,     // pipe-separated "P:0|P:1"
    val comments: String?,
    val progressRating: Int,   // 1-5
    val createdAt: Long
) {
    val exerciseKeys: List<String>
        get() = if (exercises.isBlank()) emptyList() else exercises.split("|").filter { it.isNotBlank() }
}

data class SessionView(val session: Session, val studentUser: User)

// ── Домашние задания ──────────────────────────────────────────────────────────

enum class HomeworkStatus(val displayName: String) {
    ACTIVE("Активное"),
    OVERDUE("Просрочено"),
    COMPLETED("Выполнено")
}

data class Homework(
    val id: Int,
    val studentUserId: Int,
    val logopedId: Int,
    val exercises: String,
    val dueDate: String,        // yyyy-MM-dd
    val parentComment: String?,
    val status: HomeworkStatus, // stored: ACTIVE or COMPLETED; OVERDUE computed
    val studentComment: String?,
    val assignedAt: Long,
    val completedAt: Long?
) {
    val exerciseKeys: List<String>
        get() = if (exercises.isBlank()) emptyList() else exercises.split("|").filter { it.isNotBlank() }

    val effectiveStatus: HomeworkStatus
        get() {
            if (status == HomeworkStatus.COMPLETED) return HomeworkStatus.COMPLETED
            return try {
                if (dueDate < LocalDate.now().toString()) HomeworkStatus.OVERDUE
                else HomeworkStatus.ACTIVE
            } catch (_: Exception) { status }
        }
}

data class HomeworkView(val homework: Homework, val studentUser: User)
