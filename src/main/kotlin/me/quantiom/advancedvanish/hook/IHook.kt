package me.quantiom.advancedvanish.hook

import org.bukkit.event.Listener

interface IHook : Listener {
    fun getID(): String

    fun onEnable() {}
    fun onDisable() {}
}