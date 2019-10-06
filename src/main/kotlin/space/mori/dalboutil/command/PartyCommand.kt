package space.mori.dalboutil.command

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import space.mori.dalboutil.DalboUtil.Companion.instance
import space.mori.dalboutil.party.Party
import space.mori.dalboutil.party.PartyManager
import space.mori.dalboutil.party.currentParty

object PartyCommand : CommandBase() {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty() || args[0] == "help") {
            sender.sendMessage("${instance.name} ${command.name}")
            for (subCommand in SubCommands.values) {
                sender.sendMessage("${subCommand.parameter} - ${subCommand.description}")
            }

            return true
        }

        val subCommand = SubCommands[args[0]]

        return if (subCommand != null) {
            subCommand.CommandExecutor(sender, command, label, args)
        } else {
            sender.sendMessage("${args[0]} does not exist.")

            true
        }
    }

    val invitedPlayers: MutableMap<Player, Party> = mutableMapOf()

    override val SubCommands = listOf (
        object: SubCommand() {
            override val name = "create"
            override val parameter = "/party create"
            override val description = "create party"
            override fun CommandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val party = PartyManager.createParty(sender as Player)
                if (party != null) {
                    sender.sendMessage("success! you created party ${party.Id}")
                } else {
                    sender.sendMessage("you already join party")
                }

                return true
            }

            override fun TabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return mutableListOf()
            }
        },
        object: SubCommand() {
            override val name = "list"
            override val parameter = "/party list"
            override val description = "Party member view"
            override fun CommandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val party = PartyManager.PartyList.firstOrNull() { it.players.contains(sender) }

                if (party != null) {
                    sender.sendMessage("your party is ${party.Id} (${party.players.size}/4")
                    sender.sendMessage("party leader: ${party.leader.displayName}")
                    party.players.map { if (it != party.leader) sender.sendMessage("${it.displayName}") }
                } else {
                    sender.sendMessage("you is not join party")
                }
                return true
            }
            override fun TabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return mutableListOf()
            }
        },
        object: SubCommand() {
            override val name = "invite"
            override val parameter = "/party invite <player>"
            override val description = "Invite the player in my party"
            override fun CommandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val party = (sender as Player).currentParty

                if (party != null) {
                    if (party.leader != sender) {
                        sender.sendMessage("you is not leader")
                    } else if (party.players.size >= 4) {
                        sender.sendMessage("your party is full")
                    } else {
                        val invitePlayer = Bukkit.getPlayer(args[1])
                        when {
                            invitedPlayers[invitePlayer] != null -> sender.sendMessage("player ${invitePlayer!!.name} is already invited")
                            invitePlayer?.currentParty != null -> sender.sendMessage("player ${invitePlayer.name} is already join party")
                            invitedPlayers.values.contains(party) -> sender.sendMessage("you already send invite")
                            else -> {
                                sender.sendMessage("player ${invitePlayer!!.name} invited!")
                                invitePlayer.sendMessage("you invited party ${party.Id} with ${sender.name}.")
                                invitePlayer.sendMessage("type /party accept to accept invite, /party deny to deny invite")
                                invitePlayer.sendMessage("auto deny to 120secs")

                                invitedPlayers[invitePlayer] = party

                                instance.server.scheduler.scheduleSyncDelayedTask(instance, Runnable {
                                    if (invitedPlayers[invitePlayer] == party) {
                                        sender.sendMessage("invite for ${invitePlayer.name} is refused.")
                                        invitePlayer.sendMessage("invite from ${sender.name} is refused.")
                                        invitedPlayers.remove(invitePlayer)
                                    }
                                }, 20L * 120)
                            }
                        }
                    }
                } else {
                    sender.sendMessage("you isan't party leader or join party")
                }
                return true
            }
            override fun TabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return Bukkit.getOnlinePlayers().filter { it.currentParty == null }.map { it.name }.toMutableList()
            }
        },
        object: SubCommand() {
            override val name = "accept"
            override val parameter = "/party accept"
            override val description = "Accept the party request"
            override fun CommandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                if (invitedPlayers.keys.contains(sender)) {
                    when {
                        invitedPlayers[sender] == null -> sender.sendMessage("party is not avaiable")
                        invitedPlayers[sender]?.players?.size!! >= 4 -> sender.sendMessage("party is full")
                        else -> {
                            sender.sendMessage("join party ${invitedPlayers[sender]!!.Id}")
                            invitedPlayers[sender]!!.leader.sendMessage("${sender.name} is join the party")

                            invitedPlayers[sender]!!.addPlayer(sender as Player)
                            invitedPlayers.remove(sender)
                        }
                    }
                } else {
                    sender.sendMessage("you is not invited")
                }
                return true
            }
            override fun TabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return mutableListOf()
            }
        },
        object: SubCommand() {
            override val name = "deny"
            override val parameter = "/party deny"
            override val description = "Deny the party request"
            override fun CommandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                if (invitedPlayers.keys.contains(sender)) {
                    sender.sendMessage("deny party ${invitedPlayers[sender]!!.Id}")

                    invitedPlayers.remove(sender)
                } else {
                    sender.sendMessage("you is not invited")
                }
                return true
            }
            override fun TabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return mutableListOf()
            }
        },
        object: SubCommand() {
            override val name = "invite"
            override val parameter = "/party invite <player>"
            override val description = "Invite the player in my party"
            override fun CommandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val party = (sender as Player).currentParty

                if (party != null) {
                    if (party.leader != sender) {
                        sender.sendMessage("you is not leader")
                    } else {
                        val newLeader = Bukkit.getPlayer(args[1])

                        if (newLeader?.currentParty == null || newLeader.currentParty != party) {
                            sender.sendMessage("${args[1]} is not your party")
                        } else {
                            party.changeLeader(newLeader)

                            newLeader.sendMessage("${sender.name} is inhert party ${sender.currentParty!!.Id}")
                            sender.sendMessage("party ${sender.currentParty!!.Id} is successfuly inhert to ${newLeader.name}")
                        }
                    }
                } else {
                    sender.sendMessage("you isan't party leader or join party")
                }
                return true
            }
            override fun TabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return Bukkit.getOnlinePlayers().filter { it.currentParty == null }.map { it.name }.toMutableList()
            }
        },
        object: SubCommand() {
            override val name = "quit"
            override val parameter = "/party quit"
            override val description = "quit the party"
            override fun CommandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                when {
                    (sender as Player).currentParty == null -> sender.sendMessage("you not in party")
                    sender.currentParty!!.leader == sender -> sender.sendMessage("you is party leader. before inhert leader or remove party")
                    else -> {
                        sender.sendMessage("you leave party ${sender.currentParty!!.Id}")

                        sender.currentParty!!.players.map {
                            if (it != sender) it.sendMessage("${sender.name} is leave party")
                        }
                        sender.currentParty!!.removePlayer(sender)
                    }
                }
                return true
            }
            override fun TabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return mutableListOf()
            }
        },
        object: SubCommand() {
            override val name = "delete"
            override val parameter = "/party delete"
            override val description = "delete party"
            override fun CommandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val party = PartyManager.PartyList.firstOrNull() { it.leader == sender }

                if (party != null) {
                    sender.sendMessage("successful remove party ${party.Id}")
                    PartyManager.PartyList.remove(party)
                } else {
                    sender.sendMessage("you isen't party leader or join party")
                }
                return true
            }
            override fun TabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return mutableListOf()
            }
        }).associateBy { it.name }
}