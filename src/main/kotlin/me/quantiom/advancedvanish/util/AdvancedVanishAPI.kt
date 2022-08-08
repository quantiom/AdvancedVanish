package me.quantiom.advancedvanish.util

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import github.scarsz.discordsrv.DiscordSRV
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.event.PlayerUnVanishEvent
import me.quantiom.advancedvanish.event.PlayerVanishEvent
import me.quantiom.advancedvanish.event.PrePlayerUnVanishEvent
import me.quantiom.advancedvanish.event.PrePlayerVanishEvent
import me.quantiom.advancedvanish.hook.HooksManager
import me.quantiom.advancedvanish.permission.PermissionsManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

fun Player.isVanished() = AdvancedVanishAPI.isPlayerVanished(this)

object AdvancedVanishAPI {
    val vanishedPlayers: MutableList<UUID> = Lists.newArrayList()
    private val storedPotionEffects: MutableMap<UUID, List<PotionEffect>> = Maps.newHashMap()

    /**
     * Vanishes a player if the PrePlayerVanishEvent
     * does not get cancelled.
     *
     * @param player The player to vanish
     * @param onJoin If this is being called from the PlayerJoinEvent, used for hook/fake join and leave message functionality
     */
    fun vanishPlayer(player: Player, onJoin: Boolean = false) {
        val prePlayerVanishEvent = PrePlayerVanishEvent(player)
        Bukkit.getPluginManager().callEvent(prePlayerVanishEvent)

        if (prePlayerVanishEvent.isCancelled) return

        this.vanishedPlayers.add(player.uniqueId)

        val previousEffects: MutableList<PotionEffect> = Lists.newArrayList();

        // add potion effects
        Config.getValueOrDefault("when-vanished.give-potion-effects", Lists.newArrayList<String>())
            .map { it.split(":") }
            .filter { it.size > 1 }
            .forEach {
                PotionEffectType.values().find { e -> e.name == it[0] }?.run {
                    val currentPotionEffect = player.getPotionEffect(this)

                    if (currentPotionEffect != null) {
                        previousEffects.add(currentPotionEffect)
                    } else {
                        previousEffects.add(this.createEffect(0, 0))
                    }

                    player.addPotionEffect(this.createEffect(Integer.MAX_VALUE, it[1].toInt() - 1))
                }
            }

        if (previousEffects.isNotEmpty()) {
            this.storedPotionEffects[player.uniqueId] = previousEffects
        }

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
                DiscordSRV.getPlugin().sendLeaveMessage(player, message)
            }
        }

        if (Config.getValueOrDefault("when-vanished.fly.enable", true)) {
            player.allowFlight = true
        }

        Bukkit.getPluginManager().callEvent(PlayerVanishEvent(player))
    }

    /**
     * Vanishes a player if the PrePlayerUnVanishEvent
     * does not get cancelled.
     *
     * @param player The player to unvanish
     * @param onLeave If this is being called from the PlayerQuitEvent, used for hook/fake join and leave message functionality
     */
    fun unVanishPlayer(player: Player, onLeave: Boolean = false) {
        val prePlayerUnVanishEvent = PrePlayerUnVanishEvent(player)
        Bukkit.getPluginManager().callEvent(prePlayerUnVanishEvent)

        if (prePlayerUnVanishEvent.isCancelled) return

        this.vanishedPlayers.remove(player.uniqueId)

        this.storedPotionEffects[player.uniqueId]?.let {
            for (potionEffect in it) {
                player.removePotionEffect(potionEffect.type)

                if (potionEffect.duration != 0) {
                    player.addPotionEffect(potionEffect)
                }
            }

            this.storedPotionEffects.remove(player.uniqueId)
        }

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
                DiscordSRV.getPlugin().sendJoinMessage(player, message)
            }
        }

        Bukkit.getPluginManager().callEvent(PlayerUnVanishEvent(player))
    }

    fun refreshVanished(player: Player) {
        this.vanishedPlayers.forEach { uuid ->
            Bukkit.getPlayer(uuid)?.let {
                if (!this.canSee(player, it)) {
                    player.hidePlayer(it)
                }
            }
        }
    }

    /**
     * Checks if a player is vanished
     *
     * @param player The player to check if vanished
     */
    fun isPlayerVanished(player: Player): Boolean = this.vanishedPlayers.contains(player.uniqueId)

    /**
     * Returns true if `player` can see `target`
     *
     * @param player
     * @param target
     */
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