package me.quantiom.advancedvanish.util

import me.quantiom.advancedvanish.config.Config
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.regex.Pattern

fun String.applyPlaceholders(vararg pairs: Pair<String, String>): String {
    var intermediate = this

    pairs.forEach { (search, replacement) ->
        intermediate = intermediate.replace(search, replacement)
    }

    return intermediate
}

fun List<String>.applyPlaceholders(vararg pairs: Pair<String, String>): List<String>
        = this.map { it.applyPlaceholders(*pairs) }

fun String.color(): String {
    var message = this;
    val pattern = Pattern.compile("#[a-fA-F0-9]{6}")
    var matcher = pattern.matcher(message)

    while (matcher.find()) {
        val hexCode = message.substring(matcher.start(), matcher.end())
        val replaceSharp = hexCode.replace('#', 'x')
        val ch = replaceSharp.toCharArray()
        val builder = StringBuilder("")

        for (c in ch) {
            builder.append("&$c")
        }

        message = message.replace(hexCode, builder.toString())
        matcher = pattern.matcher(message)
    }

    return ChatColor.translateAlternateColorCodes('&', message)
}

fun CommandSender.sendConfigMessage(key: String) = Config.sendMessage(this, key)
fun CommandSender.sendConfigMessage(key: String, vararg pairs: Pair<String, String>) = Config.sendMessage(this, key, *pairs)
fun Player.sendColoredMessage(msg: String) = this.sendMessage(msg.color())