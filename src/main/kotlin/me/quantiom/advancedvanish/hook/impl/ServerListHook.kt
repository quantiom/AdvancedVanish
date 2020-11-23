package me.quantiom.advancedvanish.hook.impl

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedServerPing
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.hook.IHook
import me.quantiom.advancedvanish.util.VanishUtil

class ServerListHook : IHook {
    private val packetListener = object : PacketAdapter(
        params(
            AdvancedVanish.instance,
            PacketType.Status.Server.OUT_SERVER_INFO
        ).optionAsync()
    ) {
        override fun onPacketSending(event: PacketEvent?) {
            val ping = event!!.packet.serverPings.read(0) as WrappedServerPing

            ping.playersOnline -= VanishUtil.vanishedPlayers.size
            ping.setPlayers(ping.players.filter { VanishUtil.vanishedPlayers.find { vUUID -> vUUID == it.uuid } == null })
        }
    }

    override fun getID() = "ServerList"

    override fun onEnable() {
        ProtocolLibrary.getProtocolManager().addPacketListener(this.packetListener)
    }

    override fun onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListener(this.packetListener)
    }
}