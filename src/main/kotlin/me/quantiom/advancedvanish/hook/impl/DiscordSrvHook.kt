package me.quantiom.advancedvanish.hook.impl

import github.scarsz.discordsrv.DiscordSRV
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.event.PlayerUnVanishEvent
import me.quantiom.advancedvanish.event.PlayerVanishEvent
import me.quantiom.advancedvanish.hook.HooksManager
import me.quantiom.advancedvanish.hook.IHook
import me.quantiom.advancedvanish.util.AdvancedVanishAPI
import me.quantiom.advancedvanish.util.applyPlaceholders
import me.quantiom.advancedvanish.util.color
import me.quantiom.advancedvanish.util.colorLegacy
import org.bukkit.event.EventHandler
import org.bukkit.metadata.FixedMetadataValue

class DiscordSrvHook : IHook {
    override fun getID() = "DiscordSrv"

    // DiscordSRV uses the "vanished" metadata to check if a player is
    // vanished (https://github.com/DiscordSRV/DiscordSRV/blob/master/src/main/java/github/scarsz/discordsrv/DiscordSRV.java#L1202-L1224)
    // This avoids sending double join and leave messages on discord (for fake vanish messages)

    @EventHandler
    private fun onVanish(event: PlayerVanishEvent) {
        event.player.setMetadata("vanished", FixedMetadataValue(AdvancedVanish.instance!!, true))

        if (!event.onJoin && Config.getValueOrDefault("join-leave-messages.fake-leave-message-on-vanish.enable", false)) {
            val message = Config.getValueOrDefault(
                "join-leave-messages.fake-leave-message-on-vanish.message",
                "<yellow>%player-name% has left the game."
            ).applyPlaceholders(
                "%player-name%" to event.player.name
            ).color()

            DiscordSRV.getPlugin().sendLeaveMessage(event.player, message.colorLegacy())
        }
    }

    @EventHandler
    private fun onUnVanish(event: PlayerUnVanishEvent) {
        event.player.removeMetadata("vanished", AdvancedVanish.instance!!)

        if (!event.onLeave && Config.getValueOrDefault("join-leave-messages.fake-join-message-on-unvanish.enable", false)) {
            val message = Config.getValueOrDefault(
                "join-leave-messages.fake-join-message-on-unvanish.message",
                "<yellow>%player-name% has joined the game."
            ).applyPlaceholders(
                "%player-name%" to event.player.name
            ).color()

            DiscordSRV.getPlugin().sendJoinMessage(event.player, message.colorLegacy())
        }
    }
}