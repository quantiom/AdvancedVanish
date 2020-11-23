package me.quantiom.advancedvanish.permission

import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.permission.impl.BPermissionsHandler
import me.quantiom.advancedvanish.permission.impl.GroupManagerHandler
import me.quantiom.advancedvanish.permission.impl.LuckPermsHandler
import me.quantiom.advancedvanish.permission.impl.PermissionsExHandler
import org.bukkit.Bukkit
import java.util.logging.Level

object PermissionsManager {
    var handler: IPermissionsHandler? = null

    fun setupPermissionsHandler() {
        val usingPermissionsHandler = this.findPermissionsHandler()

        if (usingPermissionsHandler.isNotEmpty()) {
            AdvancedVanish.log(Level.INFO, "Using $usingPermissionsHandler for vanish priority.")
        } else {
            AdvancedVanish.log(Level.INFO, "Could not find a supported permissions plugin, vanish priority will not be used.")
        }
    }

    private fun findPermissionsHandler(): String {
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            this.handler = LuckPermsHandler()
            return "LuckPerms"
        }

        if (Bukkit.getPluginManager().isPluginEnabled("bPermissions")) {
            this.handler = BPermissionsHandler()
            return "bPermissions"
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PermissionsEx")) {
            this.handler = PermissionsExHandler()
            return "PermissionsEx"
        }

        if (Bukkit.getPluginManager().isPluginEnabled("GroupManager")) {
            this.handler = GroupManagerHandler()
            return "GroupManager"
        }

        return ""
    }
}