package space.mori.dalboutil.config

import space.mori.dalboutil.DalboUtil.Companion.instance
import space.mori.dalboutil.util.parseJSON
import space.mori.dalboutil.util.serializeJSON
import java.nio.file.Files
import java.nio.file.Path

open class ConfigBase<T: Any>(
    open var config: T,
    private val target: Path = instance.dataFolder.toPath().resolve("dummy.json")
) {
    internal fun load() {
        if (this.target.toFile().exists()) {
            config = parseJSON(
                Files.readAllBytes(this.target).toString(Charsets.UTF_8),
                config::class.java
            )
        }
    }

    internal fun save() {
        if (!this.target.toFile().exists()) {
            Files.createDirectories(this.target.parent)
            Files.createFile(this.target)
        }

        Files.write(this.target, config.serializeJSON().toByteArray())
    }
}

fun getTarget(src: String): Path {
    return instance.dataFolder.toPath().resolve(src)
}
fun getTarget(src: Path): Path {
    return instance.dataFolder.toPath().resolve(src)
}