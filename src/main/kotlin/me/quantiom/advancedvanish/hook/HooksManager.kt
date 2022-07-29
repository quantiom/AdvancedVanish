package me.quantiom.advancedvanish.hook

import com.google.common.collect.Lists
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.hook.impl.*
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import java.util.logging.Level

object HooksManager {
    private val hooks: MutableList<IHook> = Lists.newArrayList()

    @Suppress("UNCHECKED_CAST")
    fun setupHooks() {
        this.addHook("server-list", "ProtocolLib", "Server List", ::ServerListHook)
        this.addHook("essentials", "Essentials", "Essentials", ::EssentialsHook)
        this.addHook("placeholders", "PlaceholderAPI", "Placeholders", ::PlaceHolderApiHook)
        this.addHook("actionbar", "ActionBarAPI", "Action Bar", ::ActionBarHook)
        this.addHook("discordsrv", "DiscordSRV", "DiscordSRV", ::DiscordSrvHook)
        this.addHook("dynmap", "Dynmap", "Dynmap", ::DynmapHook)
        this.addHook("squaremap", "squaremap", "squaremap", ::SquaremapHook)
        this.addHook("libsdisguises", "LibsDisguises", "LibsDisguises", ::LibsDisguisesHook)
        this.addHook("playerparticles", "PlayerParticles", "PlayerParticles", ::PlayerParticlesHook)

        this.hooks.forEach {
            Bukkit.getServer().pluginManager.registerEvents(it, AdvancedVanish.instance!!)
            it.onEnable()
        }
    }

    fun disableHooks() {
        this.hooks.forEach {
            it.onDisable()
            HandlerList.unregisterAll(it)
        }
        this.hooks.clear()
    }

    fun reloadHooks() {
        AdvancedVanish.log(Level.INFO, "Reloading hooks...")

        this.disableHooks()
        this.setupHooks()
    }

    fun isHookEnabled(hookID: String): Boolean {
        return this.hooks.find { it.getID() == hookID } != null
    }

    private fun <T> addHook(configOption: String, dependencyName: String, hookName: String, instanciator: () -> (T)) {
        if (Config.getValueOrDefault("hooks.$configOption", false)) {
            if (!Bukkit.getPluginManager().isPluginEnabled(dependencyName) && dependencyName != "ActionBarAPI") {
                AdvancedVanish.log(Level.INFO, "$dependencyName not found, not using the $hookName hook.")
            } else {
                AdvancedVanish.log(Level.INFO, "Using $hookName hook.")
                this.hooks.add(instanciator() as IHook)
            }
        }
    }
}