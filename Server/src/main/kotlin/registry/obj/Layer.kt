package rhx.dol.registry.obj

import kotlinx.serialization.Serializable
import rhx.dol.registry.base.RegistryObject

/**
 * Layer represents a set of folders to be searched while resolving files
 */
@Serializable
data class Layer(
    override val id: String,
    override val path: String = id,
) : RegistryObject
