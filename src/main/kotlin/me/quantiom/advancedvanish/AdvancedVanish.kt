package me.quantiom.advancedvanish

import co.aikar.commands.PaperCommandManager
import me.quantiom.advancedvanish.command.VanishCommand
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.hook.HooksManager
import me.quantiom.advancedvanish.listener.VanishListener
import me.quantiom.advancedvanish.permission.PermissionsManager
import me.quantiom.advancedvanish.sync.ServerSyncManager
import me.quantiom.advancedvanish.state.VanishStateManager
import me.quantiom.advancedvanish.util.AdvancedVanishAPI
import me.quantiom.advancedvanish.util.UpdateChecker
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import net.kyori.adventure.platform.bukkit.BukkitAudiences

class AdvancedVanish : JavaPlugin() {
    companion object {
        var instance: AdvancedVanish? = null
        var commandManager: PaperCommandManager? = null
        var adventure: BukkitAudiences? = null

        fun log(level: Level, msg: String) {
            instance!!.logger.log(level, msg)
        }
    }

    override fun onEnable() {
        instance = this
        adventure = BukkitAudiences.create(this)

        commandManager = PaperCommandManager(this).also {
            it.enableUnstableAPI("help")
            it.registerCommand(VanishCommand, true)
        }

        Config.reload()

        // update checker
        if (Config.getValueOrDefault("check-for-updates", true)) {
            UpdateChecker.getVersion {
                if (it != this.description.version) {
                    this.logger.info("A new update for AdvancedVanish (v${it}) is available:")
                    this.logger.info("https://www.spigotmc.org/resources/advancedvanish.86036/")
                }
            }
        }

        this.server.pluginManager.registerEvents(ServerSyncManager, this)
        this.server.pluginManager.registerEvents(VanishListener, this)

        PermissionsManager.setupPermissionsHandler()
        HooksManager.setupHooks()
    }

    override fun onDisable() {
        adventure?.close()
        ServerSyncManager.close()
        VanishStateManager.onDisable()
        AdvancedVanishAPI.vanishedPlayers.map(Bukkit::getPlayer).forEach { AdvancedVanishAPI.unVanishPlayer(it!!) }
        HooksManager.disableHooks()
        commandManager?.unregisterCommand(VanishCommand)
    }
}