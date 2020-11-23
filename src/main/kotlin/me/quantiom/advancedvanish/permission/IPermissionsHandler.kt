package me.quantiom.advancedvanish.permission

import org.bukkit.entity.Player

interface IPermissionsHandler {
    fun getVanishPriority(player: Player): Int
}