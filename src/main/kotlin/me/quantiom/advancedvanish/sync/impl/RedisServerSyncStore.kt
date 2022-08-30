package me.quantiom.advancedvanish.sync.impl

import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.sync.IServerSyncStore
import org.bukkit.Bukkit
import redis.clients.jedis.JedisPool
import java.util.*
import java.util.logging.Level

object RedisServerSyncStore : IServerSyncStore {
    private var pool: JedisPool? = null

    override fun setup(): Boolean {
        this.pool = JedisPool(
            Config.getValueOrDefault("cross-server-support.redis.ip", "127.0.0.1"),
            Config.getValueOrDefault("cross-server-support.redis.port", 6379),
            null,
            Config.getValueOrDefault("cross-server-support.redis.auth", "").ifEmpty { null },
        )

        try {
            this.pool!!.resource?.get("test")
        } catch (e: Exception) {
            AdvancedVanish.log(Level.SEVERE, "There was an error while attempting to make a connection with Redis: ")
            e.printStackTrace()
            return false
        }

        AdvancedVanish.log(Level.INFO, "Successfully connected to Redis.")
        return true
    }

    override fun close() {
        this.pool?.close()
    }

    override fun get(key: UUID): Boolean {
        var returnValue: String? = null

        try {
            this.pool?.resource?.let { resource ->
                returnValue = resource.get(this.getPlayerKey(key))
                resource.close()
            }
        } catch (e: Exception) {
            AdvancedVanish.log(Level.SEVERE, "There was an error while attempting to make a connection with Redis: ")
            e.printStackTrace()
        }

        return returnValue?.let { it == "true" } ?: false
    }

    override fun setAsync(key: UUID, value: Boolean) {
        Bukkit.getScheduler().runTaskAsynchronously(AdvancedVanish.instance!!, Runnable {
            try {
                this.pool?.resource?.let { resource ->
                    resource.set(this.getPlayerKey(key), value.toString())
                    resource.close()
                }
            } catch (e: Exception) {
                AdvancedVanish.log(Level.SEVERE, "There was an error while attempting to make a connection with Redis: ")
                e.printStackTrace()
            }
        })
    }

    private fun getPlayerKey(uuid: UUID) = "advancedvanish-${uuid}"
}