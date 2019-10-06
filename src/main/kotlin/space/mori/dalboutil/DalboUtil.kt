package space.mori.dalboutil

import org.bukkit.plugin.java.JavaPlugin
import space.mori.dalboutil.command.PartyCommand
import space.mori.dalboutil.config.Config
import space.mori.dalboutil.party.PartyManager

class DalboUtil : JavaPlugin() {
    private lateinit var party: PartyManager

    companion object {
        lateinit var instance: DalboUtil
    }

    override fun onEnable() {
        instance = this

        this.party = PartyManager

        Config().load()

        getCommand("party")!!.run { setExecutor(PartyCommand); tabCompleter = PartyCommand }

        server.pluginManager.registerEvents(PartyManager, this)

        logger.info("enabled $name")
    }

    override fun onDisable() {
        Config().save()

        logger.info("disabled $name")
    }
}