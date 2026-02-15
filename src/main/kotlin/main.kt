import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.routing.*
import routes.registerGameRoutes
import routes.registerSoundRoutes

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)

    routing {
        // Статические файлы
        static("/static") {
            resources("static")
        }

        registerSoundRoutes()
        registerGameRoutes()
    }
}