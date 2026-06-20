package database.dao

import database.tables.Users
import models.User
import models.UserRole
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object UserDAO {

    fun findByEmail(email: String): User? = transaction {
        Users.select { Users.email eq email }
            .map { it.toUser() }
            .singleOrNull()
    }

    fun findById(id: Int): User? = transaction {
        Users.select { Users.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    fun findAll(): List<User> = transaction {
        Users.selectAll()
            .orderBy(Users.createdAt to SortOrder.DESC)
            .map { it.toUser() }
    }

    fun create(
        email: String,
        passwordHash: String,
        role: UserRole,
        firstName: String,
        lastName: String,
        middleName: String?,
        phone: String?,
        specialization: String?
    ): Int = transaction {
        Users.insert {
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
            it[Users.role] = role.name
            it[Users.firstName] = firstName
            it[Users.lastName] = lastName
            it[Users.middleName] = middleName
            it[Users.phone] = phone
            it[Users.specialization] = specialization
            it[isBlocked] = false
            it[createdAt] = System.currentTimeMillis()
        }[Users.id]
    }

    fun updateProfile(
        id: Int,
        firstName: String,
        lastName: String,
        middleName: String?,
        phone: String?,
        specialization: String?
    ) = transaction {
        Users.update({ Users.id eq id }) {
            it[Users.firstName] = firstName
            it[Users.lastName] = lastName
            it[Users.middleName] = middleName
            it[Users.phone] = phone
            it[Users.specialization] = specialization
        }
    }

    fun updatePassword(id: Int, passwordHash: String) = transaction {
        Users.update({ Users.id eq id }) {
            it[Users.passwordHash] = passwordHash
        }
    }

    fun setBlocked(id: Int, blocked: Boolean) = transaction {
        Users.update({ Users.id eq id }) {
            it[isBlocked] = blocked
        }
    }

    fun delete(userId: Int) = transaction {
        Users.deleteWhere { with(it) { Users.id eq userId } }
    }

    fun emailExists(email: String): Boolean = transaction {
        Users.select { Users.email eq email }.count() > 0L
    }

    private fun ResultRow.toUser() = User(
        id = this[Users.id],
        email = this[Users.email],
        passwordHash = this[Users.passwordHash],
        role = UserRole.valueOf(this[Users.role]),
        firstName = this[Users.firstName],
        lastName = this[Users.lastName],
        middleName = this[Users.middleName],
        phone = this[Users.phone],
        specialization = this[Users.specialization],
        isBlocked = this[Users.isBlocked],
        createdAt = this[Users.createdAt]
    )
}
