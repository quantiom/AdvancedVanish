package me.quantiom.advancedvanish.hook.impl

import com.connorlinfoot.actionbarapi.ActionBarAPI
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.event.PlayerUnVanishEvent
import me.quantiom.advancedvanish.event.PlayerVanishEvent
import me.quantiom.advancedvanish.hook.IHook
import me.quantiom.advancedvanish.util.AdvancedVanishAPI
import me.quantiom.advancedvanish.util.color
import me.quantiom.advancedvanish.util.isVanished
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable

class ActionBarHook : IHook {
    private val updateTask: BukkitRunnable =
        object : BukkitRunnable() {
            override fun run() {
                AdvancedVanishAPI.vanishedPlayers.map(Bukkit::getPlayer).map { it!! }.forEach(::sendActionBar)
            }
        }

    override fun getID() = "ActionBar"

    override fun onEnable() {
        this.updateTask.runTaskTimer(AdvancedVanish.instance!!, 0L, 40L)
    }

    override fun onDisable() {
        this.updateTask.cancel()
    }

    private fun sendActionBar(player: Player) {
        this.sendActionBarStr(player, Config.getValueOrDefault("messages.action-bar", "&cYou are in vanish."))
    }

    private fun sendActionBarStr(player: Player, str: String) {
        try {
            Class.forName("net.md_5.bungee.api.ChatMessageType")
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(str.color()))
        } catch (e: ClassNotFoundException) {
            ActionBarAPI.sendActionBar(player, str.color())
        }
    }

    @EventHandler
    private fun onVanish(event: PlayerVanishEvent) {
        this.sendActionBar(event.player)
    }

    @EventHandler
    private fun onUnVanish(event: PlayerUnVanishEvent) {
        this.sendActionBarStr(event.player, "")
    }
}