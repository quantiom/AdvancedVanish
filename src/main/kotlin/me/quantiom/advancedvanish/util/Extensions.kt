package me.quantiom.advancedvanish.util

import me.quantiom.advancedvanish.config.Config
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun String.applyPlaceholders(vararg pairs: Pair<String, String>): String {
    var intermediate = this

    pairs.forEach { (search, replacement) ->
        intermediate = intermediate.replace(search, replacement)
    }

    return intermediate
}

fun List<String>.applyPlaceholders(vararg pairs: Pair<String, String>): List<String>
        = this.map { it.applyPlaceholders(*pairs) }

fun String.color(): String = ChatColor.translateAlternateColorCodes('&', this)

fun CommandSender.sendConfigMessage(key: String) = Config.sendMessage(this, key)
fun Player.sendConfigMessage(key: String) = Config.sendMessage(player, key)
fun Player.sendConfigMessage(key: String, vararg pairs: Pair<String, String>) = Config.sendMessage(player, key, *pairs)
fun Player.sendColoredMessage(msg: String) = this.sendMessage(msg.color())