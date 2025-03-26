package rhx.dol.routing

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import rhx.dol.Logger
import rhx.dol.init.initFiles
import rhx.dol.init.initInstance
import rhx.dol.registry.GameRegistry
import rhx.dol.registry.obj.GameInstance

fun Application.editRouting() {
    routing {
        route("/edit") {
            get("/interface") {
                val instances = GameRegistry.registry
                call.respond(FreeMarkerContent("edit.ftl", mapOf("instances" to instances)))
            }
            post("/update") {
                val new = call.receive<GameInstance>()
                GameRegistry.register(new)
                call.respond(HttpStatusCode.OK)
            }
            route("/reload") {
                post {
                    initFiles()
                    call.respondRedirect("/")
                }
                post("/registry") {
                    initInstance()
                    call.respond(HttpStatusCode.OK)
                }
            }
            route("/registry/{id}") {
                get {
                    val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val instance = GameRegistry.get(id) ?: return@get call.respond(HttpStatusCode.NotFound)
                    try {
                        call.respondText(Json.encodeToString(instance), ContentType.Application.Json)
                    } catch (e: Exception) {
                        Logger.error("Failed to encode instance $id: ${e.message}", e)
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                }
                delete {
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    try {
                        if (GameRegistry.del(id)) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.NotFound)
                        }
                    } catch (e: Exception) {
                        Logger.error("Failed to delete instance $id: ${e.message}", e)
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                }
            }
        }
    }
}
