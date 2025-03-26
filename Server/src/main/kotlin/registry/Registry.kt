package rhx.dol.registry

import rhx.dol.registry.base.AbstractPathRegistry
import rhx.dol.registry.base.AbstractRegistry
import rhx.dol.registry.obj.Foundation
import rhx.dol.registry.obj.GameInstance
import rhx.dol.registry.obj.Layer
import rhx.dol.registry.obj.Mod

object FoundationRegistry : AbstractPathRegistry<Foundation>("foundation")

object GameRegistry : AbstractRegistry<GameInstance>("instance")

object LayerRegistry : AbstractPathRegistry<Layer>("layer")

object ModRegistry : AbstractPathRegistry<Mod>("mod")
