package me.quantiom.advancedvanish.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.HelpCommand
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.bukkit.contexts.OnlinePlayer
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
        "&c/vanish reload &8- &fReloads the config and hooks.",
        "&c/vanish priority &8- &fDisplays your vanish priority.",
        "&c/vanish list &8- &fDisplays a list of vanished players.",
        "&c/vanish status <player> &8- &fCheck if a player is in vanish.",
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

    @Subcommand("reloadconfig|reload")
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

        val players = AdvancedVanishAPI.vanishedPlayers.map(Bukkit::getPlayer).joinToString(", ", transform = Player::getName)

        player.sendConfigMessage("vanished-list", "%vanished-players%" to if (players.isEmpty()) "None" else players)
    }

    @Subcommand("status")
    private fun onStatusCommand(player: Player, target: OnlinePlayer) {
        if (!permissionCheck(player, "permissions.status-command", "advancedvanish.status-command")) return

        player.sendConfigMessage("vanish-status-command",
            "%target-name%" to target.player.name,
            "%vanish-status%" to if (target.player.isVanished()) "On" else "Off",
            "%vanish-status-word%" to if (target.player.isVanished()) "vanished" else "not vanished"
        )
    }

    @HelpCommand
    private fun onHelp(sender: CommandSender) {
        if (!permissionCheck(sender, "permissions.help-command", "advancedvanish.help-command")) return

        this.HELP_MESSAGE.forEach { sender.sendMessage(it.color()) }
    }

    private fun permissionCheck(sender: CommandSender, key: String, default: String): Boolean {
        if (!sender.hasPermission(Config.getValueOrDefault(key, "advancedvanish.help-command"))) {
            sender.sendConfigMessage("no-permission")
            return false
        }

        return true
    }
}