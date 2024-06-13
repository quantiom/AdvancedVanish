package me.quantiom.advancedvanish.util

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.event.PlayerUnVanishEvent
import me.quantiom.advancedvanish.event.PlayerVanishEvent
import me.quantiom.advancedvanish.event.PrePlayerUnVanishEvent
import me.quantiom.advancedvanish.event.PrePlayerVanishEvent
import me.quantiom.advancedvanish.permission.PermissionsManager
import me.quantiom.advancedvanish.state.VanishStateManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
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
        val prePlayerVanishEvent = PrePlayerVanishEvent(player, onJoin)
        Bukkit.getPluginManager().callEvent(prePlayerVanishEvent)

        if (prePlayerVanishEvent.isCancelled) return

        this.vanishedPlayers.add(player.uniqueId)
        
        // add vanished metadata to player for other plugins to use
        player.setMetadata("vanished", FixedMetadataValue(AdvancedVanish.instance!!, true))

        val previousEffects: MutableList<PotionEffect> = Lists.newArrayList();

        // add potion effects
        Config.getValueOrDefault("when-vanished.give-potion-effects", Lists.newArrayList<String>())
            .map { it.split(":") }
            .filter { it.size > 1 }
            .forEach {
                PotionEffectType.values().find { e -> e?.name == it[0] }?.run {
                    val currentPotionEffect = player.activePotionEffects.find { e -> e.type == this }

                    if (currentPotionEffect != null) {
                        previousEffects.add(currentPotionEffect)
                    } else {
                        previousEffects.add(this.createEffect(0, 0))
                    }

                    // Check server ver for impl of infinite duration (1.19.4+)
                    val duration = if (Bukkit.getVersion().contains("1.19.4") || Bukkit.getVersion().contains(" 1.2")) {
                        -1
                    } else Integer.MAX_VALUE

                    if (onJoin) {
                        Bukkit.getScheduler().runTaskLater(AdvancedVanish.instance!!, Runnable {
                            player.addPotionEffect(this.createEffect(duration, it[1].toInt() - 1))
                        }, 10L)
                    } else {
                        player.addPotionEffect(this.createEffect(duration, it[1].toInt() - 1))
                    }
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
                "<yellow>%player-name% has left the game."
            ).applyPlaceholders(
                "%player-name%" to player.name
            ).color()

            Bukkit.getOnlinePlayers().forEach { it.sendComponentMessage(message) }
        }

        if (Config.getValueOrDefault("when-vanished.fly.enable", true)) {
            player.allowFlight = true
        }

        Bukkit.getPluginManager().callEvent(PlayerVanishEvent(player, onJoin))
    }

    /**
     * Vanishes a player if the PrePlayerUnVanishEvent
     * does not get cancelled.
     *
     * @param player The player to unvanish
     * @param onLeave If this is being called from the PlayerQuitEvent, used for hook/fake join and leave message functionality
     */
    fun unVanishPlayer(player: Player, onLeave: Boolean = false) {
        val prePlayerUnVanishEvent = PrePlayerUnVanishEvent(player, onLeave)
        Bukkit.getPluginManager().callEvent(prePlayerUnVanishEvent)

        if (prePlayerUnVanishEvent.isCancelled) return

        this.vanishedPlayers.remove(player.uniqueId)

        // remove vanished metadata from player
        player.removeMetadata("vanished", AdvancedVanish.instance!!)

        VanishStateManager.interactEnabled.remove(player.uniqueId)

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

        // ignore if they are in spectator mode (allowed to fly by default)
        if (player.gameMode != GameMode.SPECTATOR && !player.hasPermission(Config.getValueOrDefault("permissions.keep-fly-on-unvanish", "advancedvanish.keep-fly"))
            && !Config.getValueOrDefault("advancedvanish.fly.keep-on-unvanish", false)) {
            player.isFlying = false
            player.allowFlight = false
        }

        if (!onLeave && Config.getValueOrDefault("join-leave-messages.fake-join-message-on-unvanish.enable", false)) {
            val message = Config.getValueOrDefault(
                "join-leave-messages.fake-join-message-on-unvanish.message",
                "<yellow>%player-name% has joined the game."
            ).applyPlaceholders(
                "%player-name%" to player.name
            ).color()

            Bukkit.getOnlinePlayers().forEach { it.sendComponentMessage(message) }
        }

        Bukkit.getPluginManager().callEvent(PlayerUnVanishEvent(player, onLeave))
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