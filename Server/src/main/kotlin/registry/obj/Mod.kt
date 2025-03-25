package rhx.dol.registry.obj

import rhx.dol.registry.base.RegistryObject

data class Mod(
    override val id: String,
    override val path: String,
) : RegistryObject
