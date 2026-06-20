package database.tables

import org.jetbrains.exposed.sql.Table

object Sessions : Table("sessions") {
    val id             = integer("id").autoIncrement()
    val studentUserId  = integer("student_user_id").references(Users.id)
    val logopedId      = integer("logoped_id").references(Users.id)
    val sessionDate    = varchar("session_date", 20)   // yyyy-MM-dd
    val exercises      = text("exercises")              // pipe-separated keys
    val comments       = text("comments").nullable()
    val progressRating = integer("progress_rating").default(3)
    val createdAt      = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

object Homeworks : Table("homeworks") {
    val id             = integer("id").autoIncrement()
    val studentUserId  = integer("student_user_id").references(Users.id)
    val logopedId      = integer("logoped_id").references(Users.id)
    val exercises      = text("exercises")
    val dueDate        = varchar("due_date", 20)        // yyyy-MM-dd
    val parentComment  = text("parent_comment").nullable()
    val status         = varchar("status", 20).default("ACTIVE")
    val studentComment = text("student_comment").nullable()
    val assignedAt     = long("assigned_at")
    val completedAt    = long("completed_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
