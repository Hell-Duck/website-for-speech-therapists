package database.dao

import database.tables.Homeworks
import database.tables.Sessions
import database.tables.Users
import models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object SessionDAO {

    fun findById(id: Int): Session? = transaction {
        Sessions.select { Sessions.id eq id }.map { it.toSession() }.singleOrNull()
    }

    fun findViewById(id: Int): SessionView? = transaction {
        Sessions.join(Users, JoinType.INNER, Sessions.studentUserId, Users.id)
            .select { Sessions.id eq id }
            .map { SessionView(it.toSession(), it.toUser()) }.singleOrNull()
    }

    fun findViewsByLogoped(logopedId: Int, studentUserId: Int? = null): List<SessionView> = transaction {
        Sessions.join(Users, JoinType.INNER, Sessions.studentUserId, Users.id)
            .select {
                if (studentUserId != null)
                    (Sessions.logopedId eq logopedId) and (Sessions.studentUserId eq studentUserId)
                else
                    Sessions.logopedId eq logopedId
            }
            .orderBy(Sessions.sessionDate to SortOrder.DESC, Sessions.createdAt to SortOrder.DESC)
            .map { SessionView(it.toSession(), it.toUser()) }
    }

    fun findAllViews(studentUserId: Int? = null): List<SessionView> = transaction {
        Sessions.join(Users, JoinType.INNER, Sessions.studentUserId, Users.id)
            .select { if (studentUserId != null) Sessions.studentUserId eq studentUserId else Op.TRUE }
            .orderBy(Sessions.sessionDate to SortOrder.DESC)
            .map { SessionView(it.toSession(), it.toUser()) }
    }

    fun create(
        studentUserId: Int, logopedId: Int,
        sessionDate: String, exercises: String,
        comments: String?, progressRating: Int
    ): Int = transaction {
        Sessions.insert {
            it[Sessions.studentUserId]  = studentUserId
            it[Sessions.logopedId]      = logopedId
            it[Sessions.sessionDate]    = sessionDate
            it[Sessions.exercises]      = exercises
            it[Sessions.comments]       = comments
            it[Sessions.progressRating] = progressRating
            it[Sessions.createdAt]      = System.currentTimeMillis()
        }[Sessions.id]
    }

    fun update(id: Int, sessionDate: String, exercises: String, comments: String?, progressRating: Int) = transaction {
        Sessions.update({ Sessions.id eq id }) {
            it[Sessions.sessionDate]    = sessionDate
            it[Sessions.exercises]      = exercises
            it[Sessions.comments]       = comments
            it[Sessions.progressRating] = progressRating
        }
    }

    fun delete(sessionId: Int) = transaction {
        Sessions.deleteWhere { with(it) { Sessions.id eq sessionId } }
    }

    private fun ResultRow.toSession() = Session(
        id             = this[Sessions.id],
        studentUserId  = this[Sessions.studentUserId],
        logopedId      = this[Sessions.logopedId],
        sessionDate    = this[Sessions.sessionDate],
        exercises      = this[Sessions.exercises],
        comments       = this[Sessions.comments],
        progressRating = this[Sessions.progressRating],
        createdAt      = this[Sessions.createdAt]
    )

    private fun ResultRow.toUser() = User(
        id = this[Users.id], email = this[Users.email],
        passwordHash = this[Users.passwordHash],
        role = UserRole.valueOf(this[Users.role]),
        firstName = this[Users.firstName], lastName = this[Users.lastName],
        middleName = this[Users.middleName], phone = this[Users.phone],
        specialization = this[Users.specialization],
        isBlocked = this[Users.isBlocked], createdAt = this[Users.createdAt]
    )
}

object HomeworkDAO {

    fun findById(id: Int): Homework? = transaction {
        Homeworks.select { Homeworks.id eq id }.map { it.toHomework() }.singleOrNull()
    }

    fun findViewsByLogoped(logopedId: Int): List<HomeworkView> = transaction {
        Homeworks.join(Users, JoinType.INNER, Homeworks.studentUserId, Users.id)
            .select { Homeworks.logopedId eq logopedId }
            .orderBy(Homeworks.assignedAt to SortOrder.DESC)
            .map { HomeworkView(it.toHomework(), it.toUser()) }
    }

    fun findAllViews(): List<HomeworkView> = transaction {
        Homeworks.join(Users, JoinType.INNER, Homeworks.studentUserId, Users.id)
            .selectAll()
            .orderBy(Homeworks.assignedAt to SortOrder.DESC)
            .map { HomeworkView(it.toHomework(), it.toUser()) }
    }

    fun findByStudent(studentUserId: Int): List<Homework> = transaction {
        Homeworks.select { Homeworks.studentUserId eq studentUserId }
            .orderBy(Homeworks.assignedAt to SortOrder.DESC)
            .map { it.toHomework() }
    }

    fun create(
        studentUserId: Int, logopedId: Int,
        exercises: String, dueDate: String, parentComment: String?
    ): Int = transaction {
        Homeworks.insert {
            it[Homeworks.studentUserId]  = studentUserId
            it[Homeworks.logopedId]      = logopedId
            it[Homeworks.exercises]      = exercises
            it[Homeworks.dueDate]        = dueDate
            it[Homeworks.parentComment]  = parentComment
            it[Homeworks.status]         = "ACTIVE"
            it[Homeworks.studentComment] = null
            it[Homeworks.assignedAt]     = System.currentTimeMillis()
            it[Homeworks.completedAt]    = null
        }[Homeworks.id]
    }

    fun markCompleted(id: Int, studentComment: String?) = transaction {
        Homeworks.update({ Homeworks.id eq id }) {
            it[Homeworks.status]         = "COMPLETED"
            it[Homeworks.studentComment] = studentComment
            it[Homeworks.completedAt]    = System.currentTimeMillis()
        }
    }

    fun delete(homeworkId: Int) = transaction {
        Homeworks.deleteWhere { with(it) { Homeworks.id eq homeworkId } }
    }

    private fun ResultRow.toHomework() = Homework(
        id             = this[Homeworks.id],
        studentUserId  = this[Homeworks.studentUserId],
        logopedId      = this[Homeworks.logopedId],
        exercises      = this[Homeworks.exercises],
        dueDate        = this[Homeworks.dueDate],
        parentComment  = this[Homeworks.parentComment],
        status         = HomeworkStatus.valueOf(this[Homeworks.status]),
        studentComment = this[Homeworks.studentComment],
        assignedAt     = this[Homeworks.assignedAt],
        completedAt    = this[Homeworks.completedAt]
    )

    private fun ResultRow.toUser() = User(
        id = this[Users.id], email = this[Users.email],
        passwordHash = this[Users.passwordHash],
        role = UserRole.valueOf(this[Users.role]),
        firstName = this[Users.firstName], lastName = this[Users.lastName],
        middleName = this[Users.middleName], phone = this[Users.phone],
        specialization = this[Users.specialization],
        isBlocked = this[Users.isBlocked], createdAt = this[Users.createdAt]
    )
}
