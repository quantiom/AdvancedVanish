package me.quantiom.advancedvanish.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.hook.HooksManager
import me.quantiom.advancedvanish.permission.PermissionsManager
import me.quantiom.advancedvanish.util.AdvancedVanishAPI
import me.quantiom.advancedvanish.util.color
import me.quantiom.advancedvanish.util.isVanished
import me.quantiom.advancedvanish.util.sendConfigMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("vanish|advancedvanish|v")
object VanishCommand : BaseCommand() {
    private val HELP_MESSAGE = listOf(
        "",
        "&c&m----------&c&l AdvancedVanish &c&m----------",
        "&c/vanish &8- &fToggle vanish.",
        "&c/vanish version &8- &fShows the version of the plugin.",
        "&c/vanish reload &8- &fReloads the config and hooks.",
        "&c/vanish priority &8- &fDisplays your vanish priority.",
        "&c/vanish list &8- &fDisplays a list of vanished players.",
        "&c/vanish status <player> &8- &fCheck if a player is in vanish.",
        "&c/vanish set <player> <on/off> &8- &fSet another player's vanish.",
        "&c/vanish toggle <player> &8- &fToggle another player's vanish.",
        "&c&m-----------------------------------",
        ""
    )

    @Default
    private fun onVanishCommand(player: Player) {
        if (!permissionCheck(player, "permissions.vanish", "advancedvanish.vanish")) return

        if (player.isVanished()) {
            AdvancedVanishAPI.unVanishPlayer(player)
            player.sendConfigMessage("vanish-off")
        } else {
            AdvancedVanishAPI.vanishPlayer(player)
            player.sendConfigMessage("vanish-on")
        }
    }

    @Subcommand("version")
    private fun onVersionCommand(sender: CommandSender) {
        if (!permissionCheck(sender, "permissions.version-command", "advancedvanish.version-command")) return

        sender.sendConfigMessage("version-command",
            "%version%" to "v${AdvancedVanish.instance!!.description.version}"
        )
    }

    @Subcommand("reload|reloadconfig")
    private fun onReloadCommand(sender: CommandSender) {
        if (!permissionCheck(sender, "permissions.reload-config-command", "advancedvanish.reload-config-command")) return

        Config.reload().also { sender.sendConfigMessage("config-reloaded") }

        HooksManager.reloadHooks()
        PermissionsManager.setupPermissionsHandler()
    }

    @Subcommand("priority")
    private fun onPriorityCommand(player: Player) {
        if (!permissionCheck(player, "permissions.priority-command", "advancedvanish.priority-command")) return

        if (PermissionsManager.handler == null) {
            player.sendConfigMessage("not-using-vanish-priority")
        } else {
            player.sendConfigMessage("vanish-priority", "%priority%" to PermissionsManager.handler!!.getVanishPriority(player).toString())
        }
    }

    @Subcommand("list")
    private fun onListCommand(player: Player) {
        if (!permissionCheck(player, "permissions.list-command", "advancedvanish.list-command")) return

        val players = AdvancedVanishAPI.vanishedPlayers.map(Bukkit::getPlayer).map { it!! }.joinToString(", ", transform = Player::getName)

        player.sendConfigMessage("vanished-list", "%vanished-players%" to if (players.isEmpty()) "None" else players)
    }

    @Subcommand("status")
    @Syntax("<player>")
    @CommandCompletion("@players")
    private fun onStatusCommand(sender: CommandSender, target: OnlinePlayer) {
        if (!permissionCheck(sender, "permissions.status-command", "advancedvanish.status-command")) return

        sender.sendConfigMessage("vanish-status-command",
            "%target-name%" to target.player.name,
            "%vanish-status%" to if (target.player.isVanished()) "on" else "off",
            "%vanish-status-word%" to if (target.player.isVanished()) "vanished" else "not vanished"
        )
    }

    @Subcommand("set")
    @Syntax("<player> <on/off>")
    @CommandCompletion("@players")
    private fun onSetCommand(sender: CommandSender, target: OnlinePlayer, status: String) {
        if (!permissionCheck(sender, "permissions.set-other-command", "advancedvanish.set-other-command")) return

        val toChange = status.lowercase() == "on" || status.lowercase() == "true"
        var sendAlready = false

        println(toChange)

        if (target.player.isVanished()) {
            if (toChange) {
                sendAlready = true
            } else {
                AdvancedVanishAPI.unVanishPlayer(target.player)
            }
        } else {
            if (!toChange) {
                sendAlready = true
            } else {
                AdvancedVanishAPI.vanishPlayer(target.player)
            }
        }

        if (sendAlready) {
            sender.sendConfigMessage("vanish-set-other-command-already",
                "%target-name%" to target.player.name,
                "%vanish-status%" to if (target.player.isVanished()) "on" else "off",
                "%vanish-status-word%" to if (target.player.isVanished()) "vanished" else "not vanished"
            )
        } else {
            sender.sendConfigMessage("vanish-set-other-command",
                "%target-name%" to target.player.name,
                "%vanish-status%" to if (target.player.isVanished()) "on" else "off",
                "%vanish-status-word%" to if (target.player.isVanished()) "vanished" else "not vanished"
            )
        }
    }

    @Subcommand("toggle")
    @Syntax("<player>")
    @CommandCompletion("@players")
    private fun onToggleCommand(sender: CommandSender, target: OnlinePlayer) {
        if (!permissionCheck(sender, "permissions.toggle-other-command", "advancedvanish.toggle-other-command")) return

        if (target.player.isVanished()) {
            AdvancedVanishAPI.unVanishPlayer(target.player)
        } else {
            AdvancedVanishAPI.vanishPlayer(target.player)
        }

        sender.sendConfigMessage("vanish-set-other-command",
            "%target-name%" to target.player.name,
            "%vanish-status%" to if (target.player.isVanished()) "on" else "off",
            "%vanish-status-word%" to if (target.player.isVanished()) "vanished" else "not vanished"
        )
    }

    @HelpCommand
    private fun onHelp(sender: CommandSender) {
        if (!permissionCheck(sender, "permissions.help-command", "advancedvanish.help-command")) return

        this.HELP_MESSAGE.forEach { sender.sendMessage(it.color()) }
    }

    private fun permissionCheck(sender: CommandSender, key: String, default: String): Boolean {
        if (!sender.hasPermission(Config.getValueOrDefault(key, default))) {
            sender.sendConfigMessage("no-permission")
            return false
        }

        return true
    }
}