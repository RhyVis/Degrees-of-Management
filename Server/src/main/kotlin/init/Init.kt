package rhx.dol.init

import com.akuleshov7.ktoml.Toml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import rhx.dol.Config
import rhx.dol.ConfigObj
import rhx.dol.Logger
import rhx.dol.registry.FoundationRegistry
import rhx.dol.registry.GameRegistry
import rhx.dol.registry.LayerRegistry
import rhx.dol.registry.ModRegistry
import rhx.dol.registry.obj.Foundation
import rhx.dol.registry.obj.GameInstance
import rhx.dol.registry.obj.Layer
import rhx.dol.registry.obj.Mod
import java.io.File

fun initFiles() {
    val configFile = File("config.toml")
    if (!configFile.exists()) {
        Logger.info("config.toml not exists, initialising.")
        configFile.outputStream().use {
            it.write(Toml.encodeToString(Config()).toByteArray())
        }
    }

    ConfigObj =
        try {
            Toml.decodeFromString(configFile.readText())
        } catch (e: Exception) {
            Logger.error("Failed to read config file on ${configFile.absolutePath}, check twice or delete it.", e)
            throw e
        }

    val modDir = File(ModRegistry.path)
    if (!modDir.exists()) {
        Logger.info("Creating mod directory")
        modDir.mkdirs()
    } else {
        // Scan mod dir
        ModRegistry.clear()
        modDir.listFiles { it.isFile && it.extension.lowercase() == "zip" }.forEach { modZip ->
            Logger.debug("Found mod file: ${modZip.name}")
            ModRegistry.register(Mod(modZip.nameWithoutExtension, modZip.name))
        }
        Logger.info("Loaded mods: [${ModRegistry.registry.keys.joinToString(",")}]")
    }

    val foundationDir = File(FoundationRegistry.path)
    if (!foundationDir.exists()) {
        Logger.info("Creating foundation directory")
        foundationDir.mkdirs()
    } else {
        // Scan foundation file
        FoundationRegistry.clear()
        foundationDir.listFiles { it.isFile }.forEach { foFile ->
            Logger.debug("Found foundation file: ${foFile.name}")
            FoundationRegistry.register(Foundation(foFile.nameWithoutExtension, foFile.name))
        }
        Logger.info("Loaded foundations: [${FoundationRegistry.registry.keys.joinToString(",")}]")
    }

    val layerDir = File(LayerRegistry.path)
    if (!layerDir.exists()) {
        Logger.info("Creating layer directory")
        layerDir.mkdirs()
    } else {
        // Scan layer dir
        LayerRegistry.clear()
        layerDir.listFiles { it.isDirectory }.forEach { layerFolder ->
            Logger.debug("Found layer folder: ${layerFolder.name}")
            LayerRegistry.register(Layer(layerFolder.name))
        }
        Logger.info("Loaded layers: [${LayerRegistry.registry.keys.joinToString(",")}]")
    }

    val instanceDir = File(GameRegistry.path)
    if (!instanceDir.exists()) {
        Logger.info("Creating instance directory")
        instanceDir.mkdirs()
    } else {
        // Scan instance dir
        GameRegistry.clear()
        instanceDir.listFiles { it.isFile && it.extension.lowercase() == "json" }.forEach { instanceConf ->
            Logger.debug("Found instance file: ${instanceConf.name}")
            val instance =
                try {
                    Json.decodeFromString<GameInstance>(instanceConf.readText())
                } catch (e: Exception) {
                    Logger.error("Caught error in reading ${instanceConf.absolutePath}", e)
                    return@forEach
                }
            GameRegistry.register(instance)
        }
        Logger.info("Loaded instances: [${GameRegistry.registry.keys.joinToString(",")}]")
    }

    val saveDir = File(ConfigObj.saveDir)
    if (!saveDir.exists()) {
        Logger.info("Creating save directory")
        saveDir.mkdirs()
    }

    Logger.info("Initialization complete.")
}
