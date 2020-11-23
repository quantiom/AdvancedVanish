package me.quantiom.advancedvanish.util

import com.google.common.collect.Lists
import github.scarsz.discordsrv.DiscordSRV
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.event.*
import me.quantiom.advancedvanish.hook.HooksManager
import me.quantiom.advancedvanish.permission.PermissionsManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

fun Player.isVanished() = VanishUtil.isPlayerVanished(this)

object VanishUtil {
    val vanishedPlayers: MutableList<UUID> = Lists.newArrayList()

    fun vanishPlayer(player: Player, onJoin: Boolean = false) {
        val prePlayerVanishEvent = PrePlayerVanishEvent(player)
        Bukkit.getPluginManager().callEvent(prePlayerVanishEvent)

        if (prePlayerVanishEvent.isCancelled) return

        this.vanishedPlayers.add(player.uniqueId)
        if (Config.getValueOrDefault("when-vanished.give-invisibility", false))
            player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 1, false))

        val usePriority = Config.usingPriorities && PermissionsManager.handler != null
        val playerPriority = PermissionsManager.handler?.getVanishPriority(player)

        Bukkit.getOnlinePlayers()
            .filter { it.uniqueId != player.uniqueId }
            .forEach {
                if (usePriority && it.hasPermission(Config.getValueOrDefault(
                        "permissions.vanish",
                        "advancedvanish.vanish"
                    ))) {
                    val pPriority = PermissionsManager.handler!!.getVanishPriority(it)

                    if (pPriority < playerPriority!!) {
                        it.hidePlayer(player)
                    }
                } else {
                    it.hidePlayer(player)
                }
            }

        if (!onJoin && Config.getValueOrDefault("join-leave-messages.fake-leave-message-on-vanish.enable", false)) {
            val message = Config.getValueOrDefault(
                "join-leave-messages.fake-leave-message-on-vanish.message",
                "&e%player-name% has left the game."
            ).applyPlaceholders(
                "%player-name%" to player.name
            ).color()

            Bukkit.broadcastMessage(message)

            if (HooksManager.isHookEnabled("DiscordSrv")) {
                DiscordSRV.getPlugin().sendJoinMessage(player, message)
            }
        }

        if (Config.getValueOrDefault("when-vanished.fly.enable", true)) {
            player.allowFlight = true
        }

        Bukkit.getPluginManager().callEvent(PlayerVanishEvent(player))
    }

    fun unVanishPlayer(player: Player) = this.unVanishPlayer(player, false)
    fun unVanishPlayer(player: Player, onLeave: Boolean = false) {
        val prePlayerUnVanishEvent = PrePlayerUnVanishEvent(player)
        Bukkit.getPluginManager().callEvent(prePlayerUnVanishEvent)

        if (prePlayerUnVanishEvent.isCancelled) return

        this.vanishedPlayers.remove(player.uniqueId)
        player.removePotionEffect(PotionEffectType.INVISIBILITY)

        Bukkit.getOnlinePlayers()
            .forEach {
                it.showPlayer(player)
            }

        if (!player.hasPermission(Config.getValueOrDefault("permissions.keep-fly-on-unvanish", "advancedvanish.keep-fly"))
            && !Config.getValueOrDefault("advancedvanish.fly.keep-on-unvanish", false)) {
            player.isFlying = false
            player.allowFlight = false
        }

        if (!onLeave && Config.getValueOrDefault("join-leave-messages.fake-join-message-on-unvanish.enable", false)) {
            val message = Config.getValueOrDefault(
                "join-leave-messages.fake-join-message-on-unvanish.message",
                "&e%player-name% has joined the game."
            ).applyPlaceholders(
                "%player-name%" to player.name
            ).color()

            Bukkit.broadcastMessage(message)

            if (HooksManager.isHookEnabled("DiscordSrv")) {
                DiscordSRV.getPlugin().sendLeaveMessage(player, message)
            }
        }

        Bukkit.getPluginManager().callEvent(PlayerUnVanishEvent(player))
    }

    fun refreshVanished(player: Player) {
        this.vanishedPlayers.forEach { uuid ->
            Bukkit.getPlayer(uuid)?.let { player.hidePlayer(it) }
        }
    }

    fun isPlayerVanished(player: Player): Boolean = this.vanishedPlayers.contains(player.uniqueId)

    fun canSee(player: Player, target: Player): Boolean {
        if (!target.isVanished()) return false

        if (!player.hasPermission(Config.getValueOrDefault(
                "permissions.vanish",
                "advancedvanish.vanish"
            ))) return false

        if (!Config.usingPriorities) return true

        return PermissionsManager.handler!!.getVanishPriority(player) >= PermissionsManager.handler!!.getVanishPriority(target)
    }
}