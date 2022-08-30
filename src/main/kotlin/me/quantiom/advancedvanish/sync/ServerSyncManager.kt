package me.quantiom.advancedvanish.sync

import com.google.common.collect.Maps
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.event.PlayerUnVanishEvent
import me.quantiom.advancedvanish.event.PlayerVanishEvent
import me.quantiom.advancedvanish.sync.impl.RedisServerSyncStore
import me.quantiom.advancedvanish.sync.impl.SqlServerSyncStore
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.util.*
import java.util.logging.Level

object ServerSyncManager : Listener {
    var crossServerSupportEnabled: Boolean = false
    var serverSyncStoreImpl: IServerSyncStore? = null

    var loginVanishStates: MutableMap<UUID, Boolean> = Maps.newHashMap()

    fun setup() {
        if (Config.getValueOrDefault("cross-server-support.enabled", false)) {
            serverSyncStoreImpl = when (Config.getValueOrDefault("cross-server-support.mode", "redis").lowercase()) {
                "redis" -> RedisServerSyncStore
                "sql" -> SqlServerSyncStore
                else -> {
                    AdvancedVanish.log(Level.WARNING, "Invalid mode set for cross-server-support. Please double check your config.")
                    return
                }
            }

            this.crossServerSupportEnabled = serverSyncStoreImpl!!.setup()
        } else {
            // if the cross-server-support option gets disabled when reloading
            this.serverSyncStoreImpl?.close()
            this.crossServerSupportEnabled = false
        }
    }

    fun close() {
        this.serverSyncStoreImpl?.close()
    }

    @EventHandler
    fun onAsyncJoin(event: AsyncPlayerPreLoginEvent) {
        this.get(event.uniqueId)?.let {
            this.loginVanishStates.put(event.uniqueId, it)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onVanish(event: PlayerVanishEvent) =
        run { if (!event.onJoin) this.setAsync(event.player.uniqueId, true) }

    @EventHandler(ignoreCancelled = true)
    fun onUnVanish(event: PlayerUnVanishEvent) =
        run { if (!event.onLeave) this.setAsync(event.player.uniqueId, false) }

    fun get(key: UUID): Boolean? {
        if (!this.crossServerSupportEnabled) return null
        return this.serverSyncStoreImpl?.get(key)
    }

    private fun setAsync(key: UUID, value: Boolean) {
        if (!this.crossServerSupportEnabled) return
        this.serverSyncStoreImpl?.setAsync(key, value)
    }
}