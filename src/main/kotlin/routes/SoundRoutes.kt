package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import models.UserSession
import pages.mainPage
import pages.soundPage

fun Route.registerSoundRoutes() {
    get("/sounds") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@get
        }
        call.respondText(mainPage(session), ContentType.Text.Html)
    }

    get("/sound/{name}") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@get
        }
        val sound = call.parameters["name"] ?: "P"
        call.respondText(soundPage(sound, session), ContentType.Text.Html)
    }
}
