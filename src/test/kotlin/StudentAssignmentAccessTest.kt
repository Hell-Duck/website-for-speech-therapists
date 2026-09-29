import database.dao.HomeworkDAO
import database.dao.SessionDAO
import database.dao.StudentCardDAO
import database.dao.UserDAO
import database.tables.Homeworks
import database.tables.Sessions
import database.tables.StudentCards
import database.tables.Users
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.server.testing.*
import models.StudentStatus
import models.UserRole
import models.UserSession
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import routes.registerSessionRoutes
import java.nio.file.Files
import kotlin.test.*

class StudentAssignmentAccessTest {
    @Test
    fun `logoped cannot create a session for another logoped's student`() = assignmentTest("/sessions/create") {
        assertTrue(SessionDAO.findViewsByLogoped(logopedId).isEmpty())
    }

    @Test
    fun `logoped cannot assign homework to another logoped's student`() = assignmentTest("/homework/create") {
        assertTrue(HomeworkDAO.findViewsByLogoped(logopedId).isEmpty())
    }

    @Test
    fun `logoped can create a session for own active student`() = assignmentTest("/sessions/create", ownCard = true) {
        assertEquals(1, SessionDAO.findViewsByLogoped(logopedId).size)
    }

    @Test
    fun `logoped can assign homework to own active student`() = assignmentTest("/homework/create", ownCard = true) {
        assertEquals(1, HomeworkDAO.findViewsByLogoped(logopedId).size)
    }

    private fun assignmentTest(path: String, ownCard: Boolean = false, check: Fixture.() -> Unit) {
        val dbFile = Files.createTempFile("logoped-assignment-", ".db")
        try {
            Database.connect("jdbc:sqlite:$dbFile", driver = "org.sqlite.JDBC")
            transaction { SchemaUtils.create(Users, StudentCards, Sessions, Homeworks) }
            val logopedId = user("logoped", UserRole.LOGOPED)
            val otherLogopedId = user("other", UserRole.LOGOPED)
            val studentId = user("student", UserRole.STUDENT)
            StudentCardDAO.create(studentId, if (ownCard) logopedId else otherLogopedId,
                null, null, null, StudentStatus.ACTIVE)

            testApplication {
                application {
                    install(Sessions) {
                        cookie<UserSession>("SESSION") {
                            serializer = object : SessionSerializer<UserSession> {
                                override fun serialize(session: UserSession) = "${session.userId}|${session.email}|${session.role}|${session.firstName}"
                                override fun deserialize(text: String): UserSession {
                                    val p = text.split('|')
                                    return UserSession(p[0].toInt(), p[1], p[2], p[3])
                                }
                            }
                        }
                    }
                    routing {
                        get("/test-login") {
                            call.sessions.set(UserSession(logopedId, "logoped@test.invalid", "LOGOPED", "Logoped"))
                            call.respondText("ok")
                        }
                        registerSessionRoutes()
                    }
                }
                val cookie = client.get("/test-login").headers[HttpHeaders.SetCookie]!!.substringBefore(';')
                val response = client.post(path) {
                    header(HttpHeaders.Cookie, cookie)
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody("studentUserId=$studentId&sessionDate=2026-09-29&dueDate=2026-09-29&exercises=P")
                }
                assertEquals(HttpStatusCode.OK, response.status)
                Fixture(logopedId).check()
            }
        } finally {
            Files.deleteIfExists(dbFile)
        }
    }

    private fun user(name: String, role: UserRole): Int =
        UserDAO.create("$name@test.invalid", "hash", role, name, "Test", null, null, null)

    private data class Fixture(val logopedId: Int)
}
