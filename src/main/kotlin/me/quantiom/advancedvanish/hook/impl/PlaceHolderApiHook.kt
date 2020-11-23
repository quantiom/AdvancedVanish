package me.quantiom.advancedvanish.hook.impl

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.hook.IHook
import me.quantiom.advancedvanish.util.AdvancedVanishAPI
import me.quantiom.advancedvanish.util.isVanished
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PlaceHolderApiHook : IHook {
    private val isVanishedPlaceholder = Config.getValueOrDefault("placeholders.is-vanished", "is_vanished")
    private val vanishedPlayersPlaceholder = Config.getValueOrDefault("placeholders.vanished-players", "vanished_players")
    private val playerCountPlaceholder = Config.getValueOrDefault("placeholders.player-count", "player_count")

    private val expansion: PlaceholderExpansion = object : PlaceholderExpansion() {
        override fun getVersion() = AdvancedVanish.instance!!.description.version
        override fun getAuthor() = "quantiom"
        override fun getIdentifier() = Config.getValueOrDefault("placeholders.identifier", "advancedvanish")
        override fun persist() = true
        override fun canRegister() = true

        override fun onPlaceholderRequest(player: Player, params: String): String? {
            return when (params.toLowerCase()) {
                isVanishedPlaceholder -> if (player.isVanished()) "Yes" else "No"
                vanishedPlayersPlaceholder -> {
                    val players = AdvancedVanishAPI.vanishedPlayers.map(Bukkit::getPlayer).joinToString(", ", transform = Player::getName)

                    if (players.isEmpty()) "None" else players
                }
                playerCountPlaceholder -> (Bukkit.getOnlinePlayers().size - AdvancedVanishAPI.vanishedPlayers.size).toString()
                else -> null
            }
        }
    }

    override fun getID() = "PlaceHolderApi"

    override fun onEnable() {
        this.expansion.register()
    }

    override fun onDisable() {
        this.expansion.unregister()
    }
}