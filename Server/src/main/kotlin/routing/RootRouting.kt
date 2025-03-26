package rhx.dol.routing

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import rhx.dol.Config
import rhx.dol.registry.GameRegistry
import java.io.File

fun Application.rootRouting() {
    routing {
        route("/") {
            get {
                val instances = GameRegistry.registry.values
                call.respond(FreeMarkerContent("index.ftl", mapOf("instances" to instances)))
            }
            get("/favicon.ico") {
                val favicon =
                    Config.javaClass.classLoader
                        .getResourceAsStream("favicon.ico")
                        ?.readBytes()
                        ?: File("favicon.ico").readBytes()
                call.respondBytes(
                    bytes = favicon,
                    contentType = ContentType.Image.XIcon,
                )
            }
        }
    }
}
