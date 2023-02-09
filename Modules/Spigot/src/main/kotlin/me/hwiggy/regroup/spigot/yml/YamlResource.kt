package me.hwiggy.regroup.spigot.yml

import me.hwiggy.regroup.api.Resource
import org.bukkit.configuration.file.YamlConfiguration

object YamlResource : Resource<YamlConfiguration> {
    override val extension = ".yml"
    override val loader = Resource.Loader { YamlConfiguration.loadConfiguration(it.toFile()) }
    override val saver = Resource.Saver { res: YamlConfiguration, path -> res.save(path.toFile()) }
}