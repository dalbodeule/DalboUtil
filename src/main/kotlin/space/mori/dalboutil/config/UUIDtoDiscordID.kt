package space.mori.dalboutil.config

object UUIDtoDiscordID: ConfigBase<MutableMap<String, String>>(
    config = mutableMapOf(),
    target = getTarget("uuidToDiscord.json")
)