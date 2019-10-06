package space.mori.dalboutil.config

object Config : ConfigBase<ConfigData>(
    config = ConfigData(),
    target = getTarget("config.json")
)

data class ConfigData(
    var debug: Boolean = false,
    var partyTeamKillBlock: Boolean = true,
    var discordToken: String = "",
    var discordGuild: Number = 0,
    var discordChannel: Number = 0
)