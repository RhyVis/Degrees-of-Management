package rhx.dol.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import rhx.dol.ConfigObj
import rhx.dol.Logger
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Route.saveRouting() {
    get("/list") {
        val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        iterSaveList(id)?.let {
            call.respond(it)
        } ?: call.respond(HttpStatusCode.BadRequest)
    }
    get("/get/{save}") {
        val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val saveId = call.parameters["save"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        fetchSaveFile(id, saveId)?.let {
            call.respondText(it)
        } ?: call.respond(HttpStatusCode.NotFound)
    }
    delete("/del/{save}") {
        val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
        val saveId = call.parameters["save"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
        deleteSaveFile(id, saveId)?.let {
            call.respondText(it)
        } ?: call.respond(HttpStatusCode.NotFound)
    }
    post("/upload") {
        val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        try {
            call.receive<Code>()
        } catch (e: Exception) {
            Logger.warn("Unexpected error while receiving save data", e)
            return@post call.respond(HttpStatusCode.BadRequest)
        }.let {
            if (saveToFile(id, it)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

@Serializable
data class Code(
    val code: String,
    val alias: String,
) {
    val aliasNoEmpty: String get() = alias.takeIf { it.isNotBlank() } ?: "anonymous"
}

private val dateFormatterCompact by lazy { DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss") }

private fun assembleSaveName(
    id: String,
    code: Code,
): String = "${ConfigObj.saveDir}/$id/${code.aliasNoEmpty}-${dateFormatterCompact.format(LocalDateTime.now())}.save"

private fun saveToFile(
    id: String,
    code: Code,
): Boolean =
    try {
        val currentSaveDir = File("${ConfigObj.saveDir}/$id")
        if (!currentSaveDir.exists()) currentSaveDir.mkdirs()
        val saveName = assembleSaveName(id, code)
        val saveFile = File(saveName)
        if (saveFile.exists()) {
            Logger.warn("Overriding existing save file: ${saveFile.absolutePath}")
        }
        saveFile.writeText(code.code)
        Logger.info("Saved file: $saveName")
        true
    } catch (e: Exception) {
        Logger.error("Failed to save file", e)
        false
    }

private fun iterSaveList(id: String): List<String>? =
    try {
        val saveDir = File(ConfigObj.saveDir).resolve(id)
        if (!saveDir.exists()) saveDir.mkdirs()
        saveDir.listFiles { it.isFile && (it.extension == "dos" || it.extension == "save") }.map { saveFile ->
            saveFile.nameWithoutExtension
        }
    } catch (e: Exception) {
        Logger.error("Failed to iterate save list", e)
        null
    }

private fun fetchSaveFile(
    id: String,
    saveId: String,
): String? =
    try {
        val name = "${ConfigObj.saveDir}/$id/$saveId"
        File("$name.dos").takeIf { it.exists() && it.isFile }?.readText()
            ?: File("$name.save").takeIf { it.exists() && it.isFile }?.readText()
    } catch (e: Exception) {
        Logger.error("Failed to fetch save file", e)
        null
    }

private fun deleteSaveFile(
    id: String,
    saveId: String,
): String? {
    return try {
        File("${ConfigObj.saveDir}/$id/$saveId.dos").takeIf { it.exists() && it.isFile }?.delete()
            ?: File("${ConfigObj.saveDir}/$id/$saveId.save").takeIf { it.exists() && it.isFile }?.delete()
            ?: return null
        Logger.info("Deleted save file: $saveId")
        "Successfully deleted save file: $saveId"
    } catch (e: Exception) {
        Logger.error("Failed to delete save file", e)
        null
    }
}
