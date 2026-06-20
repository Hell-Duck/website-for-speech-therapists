package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import models.UserSession
import pages.wordFindGame

fun Route.registerGameRoutes() {
    get("/game/word-find/{sound}") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@get
        }
        val sound = call.parameters["sound"] ?: "r"
        when (sound.lowercase()) {
            "r", "p" -> call.respondText(wordFindGame(session), ContentType.Text.Html)
            else     -> call.respondRedirect("/sounds")
        }
    }
}
