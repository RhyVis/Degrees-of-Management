package rhx.dol

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val rootDir: String = "data",
) {
    val rootDirPrefix get() = if (rootDir.isEmpty()) "" else "$rootDir/"

    val saveDir get() = "${rootDirPrefix}save"
}

@Suppress("ktlint:standard:property-naming")
lateinit var ConfigObj: Config
