package me.quantiom.advancedvanish.redis

import com.google.common.collect.Maps
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.event.PlayerUnVanishEvent
import me.quantiom.advancedvanish.event.PlayerVanishEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import redis.clients.jedis.JedisPool
import java.util.*
import java.util.logging.Level

object RedisManager : Listener {
    var pool: JedisPool? = null
    var proxySupportEnabled: Boolean = false

    var loginVanishStates: MutableMap<UUID, Boolean> = Maps.newHashMap()

    fun setup() {
        if (Config.getValueOrDefault("proxy-support.enabled", false)) {
            this.pool = JedisPool(
                Config.getValueOrDefault("proxy-support.redis.ip", "127.0.0.1"),
                Config.getValueOrDefault("proxy-support.redis.port", 6379),
                null,
                Config.getValueOrDefault("proxy-support.redis.auth", "").ifEmpty { null },
            )

            try {
                this.pool!!.resource.get("test")
            } catch (e: Exception) {
                this.pool = null
                AdvancedVanish.log(Level.SEVERE, "There was an error while attempting to make a connection with Redis: ")
                e.printStackTrace()
                return
            }

            this.proxySupportEnabled = true
            AdvancedVanish.log(Level.INFO, "Successfully connected to Redis.")
        } else {
            // if the proxy-support option gets disabled when reloading
            this.pool = null
            this.proxySupportEnabled = false
        }
    }

    fun close() {
        this.pool?.close()
    }

    @EventHandler
    fun onAsyncJoin(event: AsyncPlayerPreLoginEvent) {
        val got = this.get(this.getPlayerKey(event.uniqueId))?.let {
            this.loginVanishStates.put(event.uniqueId, it == "true")
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onVanish(event: PlayerVanishEvent) =
        run { if (!event.onJoin) this.setAsync(this.getPlayerKey(event.player.uniqueId), "true") }

    @EventHandler(ignoreCancelled = true)
    fun onUnVanish(event: PlayerUnVanishEvent) =
        run { if (!event.onLeave) this.setAsync(this.getPlayerKey(event.player.uniqueId), "false") }

    fun get(key: String): String? {
        var returnValue: String? = null

        if (this.proxySupportEnabled && this.pool != null) {
            try {
                this.pool!!.resource.let { resource ->
                    returnValue = resource.get(key)
                    resource.close()
                }
            } catch (e: Exception) {
                AdvancedVanish.log(
                    Level.SEVERE,
                    "There was an error while attempting to make a connection with Redis: "
                )
                e.printStackTrace()
            }
        }

        return returnValue
    }

    private fun setAsync(key: String, value: String) {
        if (!this.proxySupportEnabled || this.pool == null) return

        Bukkit.getScheduler().runTaskAsynchronously(AdvancedVanish.instance!!, Runnable {
            try {
                this.pool!!.resource.let { resource ->
                    resource.set(key, value)
                    resource.close()
                }
            } catch (e: Exception) {
                AdvancedVanish.log(
                    Level.SEVERE,
                    "There was an error while attempting to make a connection with Redis: "
                )
                e.printStackTrace()
            }
        })
    }

    private fun getPlayerKey(uuid: UUID) = "advancedvanish-${uuid}"
}