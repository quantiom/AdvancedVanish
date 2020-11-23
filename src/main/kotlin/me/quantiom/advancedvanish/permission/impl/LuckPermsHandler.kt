package me.quantiom.advancedvanish.permission.impl

import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.permission.IPermissionsHandler
import net.luckperms.api.LuckPermsProvider
import org.bukkit.entity.Player

class LuckPermsHandler : IPermissionsHandler {
    override fun getVanishPriority(player: Player): Int {
        return LuckPermsProvider.get().userManager.getUser(player.uniqueId)?.run {
            return this.cachedData.metaData.getMetaValue(Config.getValueOrDefault(
                "priority.meta-key",
                "advancedvanish-priority"
            ))?.toInt() ?: 0
        } ?: 0
    }
}