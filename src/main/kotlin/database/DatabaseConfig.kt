package database

import database.dao.UserDAO
import database.tables.Homeworks
import database.tables.Sessions
import database.tables.StudentCards
import database.tables.StudentProfiles
import database.tables.Users
import models.UserRole
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object DatabaseConfig {

    fun init() {
        Database.connect(
            url = "jdbc:sqlite:logoped.db",
            driver = "org.sqlite.JDBC"
        )
        transaction {
            SchemaUtils.createMissingTablesAndColumns(Users, StudentCards, StudentProfiles, Sessions, Homeworks)
        }
        seedAdmin()
    }

    private fun seedAdmin() {
        if (!UserDAO.emailExists("admin@logoped.ru")) {
            UserDAO.create(
                email = "admin@logoped.ru",
                passwordHash = BCrypt.hashpw("admin123", BCrypt.gensalt()),
                role = UserRole.ADMIN,
                firstName = "Администратор",
                lastName = "Системы",
                middleName = null,
                phone = null,
                specialization = null
            )
        }
    }
}
