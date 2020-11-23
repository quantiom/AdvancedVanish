package me.quantiom.advancedvanish.permission.impl

import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.permission.IPermissionsHandler
import org.bukkit.entity.Player
import ru.tehkode.permissions.bukkit.PermissionsEx

class PermissionsExHandler : IPermissionsHandler {
    override fun getVanishPriority(player: Player): Int {
        return PermissionsEx.getUser(player).getOption(
            Config.getValueOrDefault(
                "priority.meta-key",
                "advancedvanish-priority"
            )).toIntOrNull() ?: 0
    }
}