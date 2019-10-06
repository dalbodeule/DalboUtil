package space.mori.dalboutil.party

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import space.mori.dalboutil.config.Config

object PartyManager : Listener {
    internal val PartyList: MutableList<Party> = mutableListOf()

    internal fun createParty(player: Player): Party? {
        return if (player.currentParty == null) {
            val party = Party(player, arrayListOf(player))
            PartyList.add(party)
            party
        } else {
            null
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    internal fun onPlayerHit(event: EntityDamageByEntityEvent) {
        val victim = if (event.entity is Player) event.entity as Player else return
        val attacker = if (event.damager is Player) event.damager as Player else return

        if (victim.currentParty == attacker.currentParty) {
            if (Config().config.partyTeamKillBlock) {
                event.isCancelled = true
            }
        }
    }
}

val Player.currentParty: Party?
    get() {
        return PartyManager.PartyList.firstOrNull { party -> party.players.filter { p -> p == player }.contains(this.player) }
    }


data class Party(
    var leader: Player,
    val players: ArrayList<Player>,
    val Id: Any = run {
        val id = (0..1000).random()
        while (true) {
            if (PartyManager.PartyList.none { it.Id == id }) {
                return@run id
            }
        }
    }
) {
    internal fun addPlayer(player: Player): Boolean {
        return if (players.size >= 4) {
            false
        } else {
            players.add(player)
            @Suppress("DEPRECATION")
            true
        }
    }

    internal fun removePlayer(player: Player): Boolean {
        return if (player in players) {
            players.remove(player)
            @Suppress("DEPRECATION")

            true
        } else {
            false
        }
    }

    internal fun changeLeader(player: Player): Boolean {
        return if (players.contains(player)) {
            leader = player
            true
        } else {
            false
        }
    }
}