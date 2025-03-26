package rhx.dol.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
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
import java.io.File

fun Application.editRouting() {
    routing {
        route("/edit") {
            get("/interface") {
                val instances = GameRegistry.registry
                call.respond(FreeMarkerContent("edit.ftl", mapOf("instances" to instances)))
            }
            post("/update") {
                try {
                    val new = call.receive<GameInstance>()
                    val newId = new.id
                    val instanceFile = File("${GameRegistry.path}/$newId.json")
                    if (instanceFile.exists()) {
                        Logger.info("Updating instance: $newId")
                        instanceFile.writeText(Json.encodeToString(new))
                    } else {
                        Logger.info("Creating new instance: $newId")
                        instanceFile.writeText(Json.encodeToString(new))
                    }
                    initInstance()
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    Logger.error("Failed to update instance", e)
                    call.respond(HttpStatusCode.InternalServerError)
                }
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
                        call.respond(instance)
                    } catch (e: Exception) {
                        Logger.error("Failed to encode instance $id: ${e.message}", e)
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                }
                delete {
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    try {
                        File("${GameRegistry.path}/$id.json").delete()
                        initInstance()
                        call.respond(HttpStatusCode.OK)
                    } catch (e: Exception) {
                        Logger.error("Failed to delete instance $id: ${e.message}", e)
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                }
            }
        }
    }
}
