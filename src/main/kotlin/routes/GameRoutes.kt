package routes

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.http.*
import pages.wordFindGame

fun Route.registerGameRoutes() {
    get("/game/word-find/{sound}") {
        val sound = call.parameters["sound"] ?: "r"

        when (sound.lowercase()) {
            "r", "p" -> {
                call.respondText(wordFindGame(), ContentType.Text.Html)
            }
            else -> {
                call.respondRedirect("/")
            }
        }
    }
}