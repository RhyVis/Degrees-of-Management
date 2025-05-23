package rhx.dol.registry.obj

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import rhx.dol.registry.FoundationRegistry
import rhx.dol.registry.LayerRegistry
import rhx.dol.registry.ModRegistry
import rhx.dol.registry.base.RegistryObject
import rhx.dol.registry.base.RegistryPathObject

data class Mod(
    override val id: String,
    override val path: String,
) : RegistryPathObject

data class Foundation(
    override val id: String,
    override val path: String,
) : RegistryPathObject

@Serializable
data class Layer(
    override val id: String,
    override val path: String = id,
) : RegistryPathObject

@Serializable
data class GameInstance(
    override val id: String,
    val name: String = "",
    val foundation: String = "",
    val mods: List<String> = emptyList(),
    val layers: List<String> = emptyList(),
) : RegistryObject {
    val foundationFullPath: String?
        get() = FoundationRegistry.getPathStr(foundation)

    val modListJson: String
        get() =
            Json.Default.encodeToString(
                mods
                    .mapNotNull { ModRegistry.get(it)?.id?.let { "/mod/$it" } }
                    .plus("/mod/save-sync-integration"),
            )

    val layerPathReversedList: List<String>
        get() = layers.reversed().mapNotNull { LayerRegistry.getPathStr(it) }
}
