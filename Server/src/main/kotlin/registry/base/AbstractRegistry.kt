package rhx.dol.registry.base

import rhx.dol.ConfigObj

abstract class AbstractRegistry<R : RegistryObject>(
    val name: String,
) {
    private val _registry = mutableMapOf<String, R>()
    val registry: Map<String, R> get() = _registry

    val path by lazy { "${ConfigObj.rootDirPrefix}$name" }

    fun register(obj: R) {
        _registry[obj.id] = obj
    }

    fun get(id: String): R? = _registry[id]

    fun getPathStr(id: String): String? {
        val obj = get(id) ?: return null
        return "$path/${obj.path}"
    }

    fun del(id: String): Boolean = _registry.remove(id)?.let { true } ?: false

    fun clear() {
        _registry.clear()
    }
}
