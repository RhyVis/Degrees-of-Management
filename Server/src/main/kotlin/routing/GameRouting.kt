@file:Suppress("ktlint:standard:no-wildcard-imports")

package rhx.dol.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import rhx.dol.Config
import rhx.dol.Logger
import rhx.dol.registry.GameRegistry
import rhx.dol.registry.ModRegistry
import java.io.File

fun Application.gameRouting() {
    routing {
        route("/play/{id}") {
            // Respond index.html
            get("/index") {
                val id =
                    call.parameters["id"] ?: let {
                        Logger.warn("Unexpected null id")
                        return@get call.respond(HttpStatusCode.BadRequest)
                    }
                val instance =
                    GameRegistry.get(id) ?: let {
                        Logger.warn("Instance with id $id not found")
                        return@get call.respond(HttpStatusCode.NotFound)
                    }
                val foundationPath =
                    instance.foundationFullPath ?: let {
                        Logger.warn("Foundation object not found")
                        return@get call.respond(HttpStatusCode.NotFound)
                    }

                Logger.info("Responding $id instance, on $foundationPath")

                val indexContent =
                    try {
                        File(foundationPath).readText()
                    } catch (e: Exception) {
                        Logger.error("Failed to read index html", e)
                        return@get call.respond(HttpStatusCode.NotFound)
                    }

                call.respondText(indexContent, ContentType.Text.Html)
            }

            // Respond mod list requested by mod loader
            get("/modList.json") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val instance = GameRegistry.get(id) ?: return@get call.respond(HttpStatusCode.NotFound)

                call.respondText(
                    instance.modListJson,
                    ContentType.Application.Json,
                )
            }

            // Route on save handling
            route("/save-sync") {
                saveRouting()
            }

            // Fallback to layer files
            get("/{path...}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val instance = GameRegistry.get(id) ?: return@get call.respond(HttpStatusCode.NotFound)

                Logger.debug("Resolving ${call.request.uri}")

                val remainingPath = call.parameters.getAll("path")?.joinToString("/") ?: ""

                val content =
                    getFileContent(instance.layerPathReversedList, remainingPath)
                        ?: return@get call.respond(HttpStatusCode.Forbidden)

                val contentType =
                    when {
                        remainingPath.endsWith(".png") -> ContentType.Image.PNG
                        else -> ContentType.Application.OctetStream
                    }

                call.respondBytes(content, contentType)
            }
        }
        // Mod file handling
        route("/mod/{id}") {
            get {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)

                if (id == "save-sync-integration") {
                    try {
                        return@get call.respondBytes(getInternalSaveMod(), ContentType.Application.OctetStream)
                    } catch (e: Exception) {
                        Logger.error("Failed to get internal mod $id", e)
                        return@get call.respond(HttpStatusCode.InternalServerError)
                    }
                }

                val modPath = ModRegistry.getPathStr(id) ?: return@get call.respond(HttpStatusCode.NotFound)
                val contentFile =
                    try {
                        File(modPath).readBytes()
                    } catch (e: Exception) {
                        Logger.error("Failed to read file at $modPath", e)
                        return@get call.respond(HttpStatusCode.BadRequest)
                    }
                call.respondBytes(contentFile, ContentType.Application.OctetStream)
            }
        }
    }
}

private fun getFileContent(
    layerPathList: List<String>,
    relativePath: String,
): ByteArray? {
    for (basePath in layerPathList) {
        val path = "$basePath/$relativePath"
        Logger.debug("Trying to resolve $relativePath in $basePath")

        val file = File(path)
        if (file.exists() && file.isFile) {
            Logger.debug("Resolved file at $path")
            return file.readBytes()
        }
    }
    Logger.warn("File $relativePath not found in all of [${layerPathList.joinToString(",")}]")
    return null
}

private fun getInternalSaveMod(): ByteArray =
    (Thread.currentThread().contextClassLoader ?: Config.javaClass.classLoader)
        .getResourceAsStream("save-sync-integration.mod.zip")
        ?.use {
            it.readBytes()
        } ?: error("Unable to load internal save-sync-integration.mod.zip")
