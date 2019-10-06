package space.mori.dalboutil.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import space.mori.dalboutil.DalboUtil.Companion.instance
import space.mori.dalboutil.config.Config
import space.mori.dalboutil.config.UUIDtoDiscordID
import java.util.*
import javax.security.auth.login.LoginException


object Discord: Listener, ListenerAdapter() {
    private val verifyUsers: MutableMap<String, UUID> = mutableMapOf()

    private lateinit var bot: JDA
    private lateinit var commands: Map<String, DiscordCommand>

    @EventHandler(priority = EventPriority.HIGHEST)
    internal fun onJoin(event: PlayerJoinEvent) {
        if (UUIDtoDiscordID.config[event.player.uniqueId.toString()] == null) {
            var verifyCode = verifyUsers.filterValues { it == event.player.uniqueId }.map { it.key }.getOrNull(0)

            if (verifyCode == null) {
                 verifyCode = getRandomString(10)
            }

            event.player.kickPlayer("type '!verify $verifyCode' in discord channel 'verify'\nverify is refused in 120seconds.")
            verifyUsers[verifyCode] = event.player.uniqueId

            instance.server.scheduler.runTaskLater(instance, Runnable {
                if (verifyUsers[verifyCode] != null) {
                    verifyUsers.remove(verifyCode)
                }
            }, 20L*120)
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val msg = event.message.contentRaw
        val command = msg.split(" ")[0]

        if (!event.author.isBot && commands.keys.contains(command) && commands[command] != null) {
            instance.logger.info("${event.author.name} issued $command")
            commands[command]!!.execute(event)
        }
    }

    internal fun main() {
        try {
            bot = JDABuilder(Config.config.discordToken)
                .addEventListeners(Discord)
                .setActivity(Activity.playing("Minecraft"))
                .build()
            commands = listOf(
                object : DiscordCommand {
                    override val name = "!ping"
                    override fun execute(event: MessageReceivedEvent) {
                        event.channel.sendMessage("Pong!").queue()
                    }
                },
                object : DiscordCommand {
                    override val name = "!verify"
                    override fun execute(event: MessageReceivedEvent) {
                        if (
                            event.message.guild.id == Config.config.discordGuild.toString() &&
                            event.channel.id == Config.config.discordChannel.toString()
                        ) {
                            val code = event.message.contentRaw.split(" ")[1]

                            if (code in verifyUsers.keys) {
                                event.channel.sendMessage("successfully verified. ${Bukkit.getOfflinePlayer(verifyUsers[code]!!).name}")
                                    .queue()

                                UUIDtoDiscordID.config[verifyUsers[code]!!.toString()] = event.member!!.id
                                verifyUsers.remove(code)
                            } else {
                                event.channel.sendMessage("$code is not registed code").queue()
                            }
                        }
                    }
                }
            ).associateBy { it.name }
        } catch (e: LoginException) {
            instance.logger.info(e.message)
        }
    }

    internal fun disable() {
        bot.shutdown()
    }

    interface DiscordCommand {
        val name: String
        fun execute(event: MessageReceivedEvent)
    }

    private fun getRandomString(length: Int): String {
        val charPool = ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
}