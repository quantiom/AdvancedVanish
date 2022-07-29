package me.quantiom.advancedvanish.hook.impl

import me.quantiom.advancedvanish.event.PlayerUnVanishEvent
import me.quantiom.advancedvanish.event.PlayerVanishEvent
import me.quantiom.advancedvanish.hook.IHook
import org.bukkit.event.EventHandler
import xyz.jpenilla.squaremap.api.SquaremapProvider

class SquaremapHook : IHook {
    override fun getID() = "squaremap"

    private val squaremap = SquaremapProvider.get()

    @EventHandler
    private fun onVanish(event: PlayerVanishEvent) {
        this.squaremap.playerManager().hide(event.player.uniqueId)
    }

    @EventHandler
    private fun onUnVanish(event: PlayerUnVanishEvent) {
        this.squaremap.playerManager().show(event.player.uniqueId)
    }
}