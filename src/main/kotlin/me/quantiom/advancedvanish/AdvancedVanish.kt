package me.quantiom.advancedvanish

import co.aikar.commands.PaperCommandManager
import me.quantiom.advancedvanish.command.VanishCommand
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.hook.HooksManager
import me.quantiom.advancedvanish.listener.VanishListener
import me.quantiom.advancedvanish.permission.PermissionsManager
import me.quantiom.advancedvanish.state.VanishStateManager
import me.quantiom.advancedvanish.util.AdvancedVanishAPI
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class AdvancedVanish : JavaPlugin() {
    companion object {
        var instance: AdvancedVanish? = null
        var commandManager: PaperCommandManager? = null

        fun log(level: Level, msg: String) {
            instance!!.logger.log(level, msg)
        }
    }

    override fun onEnable() {
        instance = this

        commandManager = PaperCommandManager(this).also {
            it.enableUnstableAPI("help")
            it.registerCommand(VanishCommand, true)
        }

        Config.reload()
        this.server.pluginManager.registerEvents(VanishListener, this)

        PermissionsManager.setupPermissionsHandler()
        HooksManager.setupHooks()
    }

    override fun onDisable() {
        VanishStateManager.onDisable()
        AdvancedVanishAPI.vanishedPlayers.map(Bukkit::getPlayer).forEach { AdvancedVanishAPI.unVanishPlayer(it!!) }
        HooksManager.disableHooks()
        commandManager?.unregisterCommand(VanishCommand)
    }
}