@file:Suppress("ktlint:standard:no-wildcard-imports")

package rhx.dol

import freemarker.cache.ClassTemplateLoader
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.content.CachingOptions
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.FreeMarker
import io.ktor.server.netty.*
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import rhx.dol.init.initFiles
import rhx.dol.routing.editRouting
import rhx.dol.routing.gameRouting
import rhx.dol.routing.rootRouting

val Logger: Logger = LoggerFactory.getLogger("Service")

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    install(ContentNegotiation) { json() }
    install(CachingHeaders) {
        options { call, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
                else -> null
            }
        }
    }
    install(Compression)
    install(DefaultHeaders) { header("X-DOL", "DOL") }
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    initFiles()

    rootRouting()
    gameRouting()
    editRouting()
}
