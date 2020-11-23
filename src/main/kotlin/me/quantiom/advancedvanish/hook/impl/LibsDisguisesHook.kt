package me.quantiom.advancedvanish.hook.impl

import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.events.DisguiseEvent
import me.quantiom.advancedvanish.event.PlayerVanishEvent
import me.quantiom.advancedvanish.hook.IHook
import me.quantiom.advancedvanish.util.isVanished
import me.quantiom.advancedvanish.util.sendConfigMessage
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

// will catch errors since it's not supported on 1.8 but still gets enabled
class LibsDisguisesHook : IHook {
    override fun getID() = "LibsDisguises"

    @EventHandler
    private fun onVanish(event: PlayerVanishEvent) {
        try {
            if (DisguiseAPI.isDisguised(event.player)) {
                DisguiseAPI.undisguiseToAll(event.player)
                event.player.sendConfigMessage("disguise-removed-because-vanish")
            }
        } catch (ignored: Exception) {}
    }

    @EventHandler
    private fun onDisguise(event: DisguiseEvent) {
        try {
            if (event.entity is Player && (event.entity as Player).isVanished()) {
                event.isCancelled = true
                event.entity.sendConfigMessage("disguise-removed-because-vanish")
            }
        } catch (ignored: Exception) {}
    }
}