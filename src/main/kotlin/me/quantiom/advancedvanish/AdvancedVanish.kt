package me.quantiom.advancedvanish

import co.aikar.commands.PaperCommandManager
import me.quantiom.advancedvanish.command.VanishCommand
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.hook.HooksManager
import me.quantiom.advancedvanish.listener.VanishListener
import me.quantiom.advancedvanish.permission.PermissionsManager
import me.quantiom.advancedvanish.util.VanishUtil
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
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

        this.saveDefaultConfig().also { Config.reload() }
        this.server.pluginManager.registerEvents(VanishListener, this)

        PermissionsManager.setupPermissionsHandler()
        HooksManager.setupHooks()

        commandManager = PaperCommandManager(this).also {
            it.enableUnstableAPI("help")
            it.registerCommand(VanishCommand, true)
        }
    }

    override fun onDisable() {
        VanishUtil.vanishedPlayers.map(Bukkit::getPlayer).forEach(VanishUtil::unVanishPlayer)
        HooksManager.disableHooks()
        commandManager?.unregisterCommand(VanishCommand)
    }
}