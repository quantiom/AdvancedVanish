package me.quantiom.advancedvanish

import co.aikar.commands.PaperCommandManager
import me.quantiom.advancedvanish.command.VanishCommand
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.hook.HooksManager
import me.quantiom.advancedvanish.listener.VanishListener
import me.quantiom.advancedvanish.permission.PermissionsManager
import me.quantiom.advancedvanish.state.VanishStateManager
import me.quantiom.advancedvanish.sync.ServerSyncManager
import me.quantiom.advancedvanish.util.AdvancedVanishAPI
import me.quantiom.advancedvanish.util.DependencyManager
import me.quantiom.advancedvanish.util.UpdateChecker
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

object AdvancedVanish {
    var instance: AdvancedVanishPlugin? = null
    var commandManager: PaperCommandManager? = null
    var adventure: BukkitAudiences? = null

    fun log(level: Level, msg: String) {
        instance!!.logger.log(level, msg)
    }

    fun onEnable(plugin: AdvancedVanishPlugin) {
        instance = plugin

        adventure = BukkitAudiences.create(plugin)

        commandManager = PaperCommandManager(plugin).also {
            it.enableUnstableAPI("help")
            it.registerCommand(VanishCommand, true)
        }

        Config.reload()

        // update checker
        if (Config.getValueOrDefault("check-for-updates", true)) {
            UpdateChecker.getVersion {
                if (it != plugin.description.version) {
                    plugin.logger.info("A new update for AdvancedVanish (v${it}) is available:")
                    plugin.logger.info("https://www.spigotmc.org/resources/advancedvanish.86036/")
                }
            }
        }

        plugin.server.pluginManager.registerEvents(ServerSyncManager, plugin)
        plugin.server.pluginManager.registerEvents(VanishListener, plugin)

        PermissionsManager.setupPermissionsHandler()
        HooksManager.setupHooks()
    }

    fun onDisable() {
        adventure?.close()
        ServerSyncManager.close()
        VanishStateManager.onDisable()
        AdvancedVanishAPI.vanishedPlayers.map(Bukkit::getPlayer).forEach { AdvancedVanishAPI.unVanishPlayer(it!!) }
        HooksManager.disableHooks()
        commandManager?.unregisterCommand(VanishCommand)
    }
}