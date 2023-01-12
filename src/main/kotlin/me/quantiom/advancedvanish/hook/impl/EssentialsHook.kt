package me.quantiom.advancedvanish.hook.impl

import com.earth2me.essentials.Essentials
import me.quantiom.advancedvanish.event.PlayerUnVanishEvent
import me.quantiom.advancedvanish.event.PlayerVanishEvent
import me.quantiom.advancedvanish.hook.IHook
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent

class EssentialsHook : IHook {
    private var essentials = Bukkit.getPluginManager().getPlugin("Essentials") as Essentials

    override fun getID() = "Essentials"

    @EventHandler
    private fun onVanish(event: PlayerVanishEvent) {
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            this.essentials.getUser(event.player).isHidden = true
        }
    }

    @EventHandler
    private fun onUnVanish(event: PlayerUnVanishEvent) {
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            this.essentials.getUser(event.player).isHidden = false
        }
    }
}