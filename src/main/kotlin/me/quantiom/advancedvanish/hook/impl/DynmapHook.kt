package me.quantiom.advancedvanish.hook.impl

import me.quantiom.advancedvanish.event.PlayerUnVanishEvent
import me.quantiom.advancedvanish.event.PlayerVanishEvent
import me.quantiom.advancedvanish.hook.IHook
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.dynmap.DynmapAPI

class DynmapHook : IHook {
    override fun getID() = "Dynmap"

    private val dynmap: DynmapAPI? = Bukkit.getPluginManager().getPlugin("Dynmap") as DynmapAPI

    @EventHandler
    private fun onVanish(event: PlayerVanishEvent) {
        this.dynmap?.setPlayerVisiblity(event.player, false)
    }

    @EventHandler
    private fun onUnVanish(event: PlayerUnVanishEvent) {
        this.dynmap?.setPlayerVisiblity(event.player, true)
    }
}