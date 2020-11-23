package me.quantiom.advancedvanish.hook.impl

import com.connorlinfoot.actionbarapi.ActionBarAPI
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.event.PlayerUnVanishEvent
import me.quantiom.advancedvanish.event.PlayerVanishEvent
import me.quantiom.advancedvanish.hook.IHook
import me.quantiom.advancedvanish.util.AdvancedVanishAPI
import me.quantiom.advancedvanish.util.color
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.scheduler.BukkitRunnable

class ActionBarHook : IHook {
    private val updateTask: BukkitRunnable =
        object : BukkitRunnable() {
            override fun run() {
                if (!Bukkit.getPluginManager().isPluginEnabled("ActionBarAPI")) return

                AdvancedVanishAPI.vanishedPlayers.map(Bukkit::getPlayer).forEach(::sendActionBar)
            }
        }

    override fun getID() = "ActionBar"

    override fun onEnable() {
        this.updateTask.runTaskTimer(AdvancedVanish.instance, 0L, 40L)
    }

    override fun onDisable() {
        this.updateTask.cancel()
    }

    private fun sendActionBar(player: Player) = ActionBarAPI.sendActionBar(player, Config.getValueOrDefault("messages.action-bar", "&cYou are in vanish.").color())

    @EventHandler
    private fun onVanish(event: PlayerVanishEvent) {
        this.sendActionBar(event.player)
    }

    @EventHandler
    private fun onUnVanish(event: PlayerUnVanishEvent) {
        ActionBarAPI.sendActionBar(event.player, "")
    }
}