package rhx.dol.registry.base

/**
 * Specifies an object that can be registered in a registry
 *
 * @property id Unique identifier for the object
 * @property path Path to the object under the registry folder
 */
interface RegistryObject {
    val id: String
    val path: String
}
