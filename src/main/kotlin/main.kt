import database.DatabaseConfig
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.sessions.*
import models.UserSession
import routes.registerAuthRoutes
import routes.registerGameRoutes
import routes.registerSoundRoutes
import routes.registerSessionRoutes
import routes.registerStudentRoutes
import java.net.URLDecoder
import java.net.URLEncoder

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(DefaultHeaders)

    install(Sessions) {
        cookie<UserSession>("SESSION") {
            serializer = object : SessionSerializer<UserSession> {
                override fun serialize(session: UserSession): String =
                    "${session.userId}|${session.email}|${session.role}|${URLEncoder.encode(session.firstName, "UTF-8")}"

                override fun deserialize(text: String): UserSession {
                    val p = text.split("|")
                    return UserSession(p[0].toInt(), p[1], p[2], URLDecoder.decode(p[3], "UTF-8"))
                }
            }
            cookie.path = "/"
            cookie.maxAgeInSeconds = 86400 * 7
            cookie.httpOnly = true
            transform(SessionTransportTransformerMessageAuthentication(
                "LogopedApp-SecretKey-2024-XYZ!".toByteArray()
            ))
        }
    }

    DatabaseConfig.init()

    routing {
        staticResources("/static", "static")
        registerAuthRoutes()
        registerSoundRoutes()
        registerGameRoutes()
        registerStudentRoutes()
        registerSessionRoutes()
    }
}
