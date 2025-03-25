package rhx.dol.routing

import io.ktor.server.application.Application
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import rhx.dol.registry.GameRegistry

fun Application.rootRouting() {
    routing {
        route("/") {
            get {
                val instances = GameRegistry.registry.values
                call.respond(FreeMarkerContent("index.ftl", mapOf("instances" to instances)))
            }
        }
    }
}
