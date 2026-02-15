package routes

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.http.*
import pages.mainPage
import pages.soundPage

fun Route.registerSoundRoutes() {
    get("/") {
        call.respondText(mainPage(), ContentType.Text.Html)
    }

    get("/sound/{name}") {
        val sound = call.parameters["name"] ?: "P"
        call.respondText(soundPage(sound), ContentType.Text.Html)
    }
}