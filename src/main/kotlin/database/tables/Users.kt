package database.tables

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 50)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val middleName = varchar("middle_name", 100).nullable()
    val phone = varchar("phone", 50).nullable()
    val specialization = varchar("specialization", 255).nullable()
    val isBlocked = bool("is_blocked").default(false)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}
