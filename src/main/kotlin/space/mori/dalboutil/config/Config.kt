package space.mori.dalboutil.config

class Config : ConfigBase<ConfigData>(
    config = ConfigData(),
    target = getTarget("config.json")
)

data class ConfigData(
    var debug: Boolean = false,
    var partyTeamKillBlock: Boolean = true
)