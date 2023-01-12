package me.quantiom.advancedvanish.util

import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
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

fun String.color(): Component {
    return MiniMessage.miniMessage().deserialize(this)
}

fun Component.colorLegacy(): String {
    return LegacyComponentSerializer.legacyAmpersand().serialize(this)
}

fun CommandSender.sendConfigMessage(key: String) = Config.sendMessage(this, key)
fun CommandSender.sendConfigMessage(key: String, vararg pairs: Pair<String, String>) = Config.sendMessage(this, key, *pairs)
fun CommandSender.sendComponentMessage(msg: Component) = AdvancedVanish.adventure!!.sender(this).sendMessage(msg)

fun Player.sendComponentMessage(msg: Component) = AdvancedVanish.adventure!!.player(this).sendMessage(msg)
fun Player.sendColoredMessage(msg: String) = this.sendComponentMessage(msg.color())