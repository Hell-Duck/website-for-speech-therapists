import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.routing.*
import routes.registerGameRoutes
import routes.registerSoundRoutes

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    routing {
        registerSoundRoutes()
        registerGameRoutes()
    }
}