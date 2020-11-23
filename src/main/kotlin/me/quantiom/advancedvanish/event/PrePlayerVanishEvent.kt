@file:JvmName("PrePlayerVanishEvent")
package me.quantiom.advancedvanish.event

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PrePlayerVanishEvent(val player: Player) : Event(), Cancellable {
    companion object {
        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }

    private var isCancelled = false

    override fun getHandlers(): HandlerList = HANDLERS

    override fun setCancelled(set: Boolean) { this.isCancelled = set }
    override fun isCancelled(): Boolean = isCancelled
}