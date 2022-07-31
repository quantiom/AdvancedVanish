package me.quantiom.advancedvanish.permission.impl

import me.quantiom.advancedvanish.permission.IPermissionsHandler
import org.anjocaido.groupmanager.GroupManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class GroupManagerHandler : IPermissionsHandler {
    private val groupManager = Bukkit.getPluginManager().getPlugin("GroupManager") as GroupManager

    override fun getVanishPriority(player: Player): Int {
        return this.groupManager.worldsHolder
            .getWorldPermissions(player)
            .getAllPlayersPermissions(player.name)
            .filter { it.startsWith("advancedvanish-priority.") }
            .mapNotNull { it.split(".")[1].toIntOrNull() }
            .maxOrNull() ?: 0
    }
}