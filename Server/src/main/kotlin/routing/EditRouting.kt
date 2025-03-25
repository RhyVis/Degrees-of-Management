package rhx.dol.routing

import io.ktor.server.application.Application
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import rhx.dol.init.initFiles

fun Application.editRouting() {
    routing {
        route("/edit") {
            post("/reload") {
                initFiles()
                call.respondRedirect("/")
            }
        }
    }
}
