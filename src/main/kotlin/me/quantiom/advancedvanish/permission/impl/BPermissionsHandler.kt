package me.quantiom.advancedvanish.permission.impl

import de.bananaco.bpermissions.api.ApiLayer
import de.bananaco.bpermissions.api.CalculableType
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.permission.IPermissionsHandler
import org.bukkit.entity.Player

class BPermissionsHandler : IPermissionsHandler {
    override fun getVanishPriority(player: Player): Int {
        return ApiLayer.getValue(player.world.name, CalculableType.USER, player.name, Config.getValueOrDefault(
            "priority.meta-key",
            "advancedvanish-priority"
        )).toIntOrNull() ?: 0
    }
}