package me.quantiom.advancedvanish.sync.impl

import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.sync.IServerSyncStore
import org.bukkit.Bukkit
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.logging.Level

object SqlServerSyncStore : IServerSyncStore {
    private var database: Database? = null

    object AdvancedVanishTable : IntIdTable() {
        val uuid: Column<String> = varchar("uuid", 36)
        val state: Column<Boolean> = bool("state")
    }

    override fun setup(): Boolean {
        val host = Config.getValueOrDefault("cross-server-support.sql.ip", "127.0.0.1")
        val port = Config.getValueOrDefault("cross-server-support.sql.port", 3306)
        val username = Config.getValueOrDefault("cross-server-support.sql.username", "root")
        val password = Config.getValueOrDefault("cross-server-support.sql.password", "")
        val database = Config.getValueOrDefault("cross-server-support.sql.database", "minecraft")

        this.database = Database.connect(
            "jdbc:mysql://$host:$port/$database",
            driver = "com.mysql.cj.jdbc.Driver",
            user = username,
            password = password
        )

        try {
            transaction {
                if (!AdvancedVanishTable.exists()) {
                    SchemaUtils.create(AdvancedVanishTable)
                }
            }
        } catch (e: Exception) {
            AdvancedVanish.log(Level.SEVERE, "There was an error while attempting to make a connection with SQL: ")
            e.printStackTrace()
            return false
        }

        AdvancedVanish.log(Level.INFO, "Successfully connected to the SQL database.")
        return true
    }

    override fun close() {
        // doesn't need to be closed from what I can tell
    }

    override fun get(key: UUID): Boolean {
        try {
            return transaction {
                return@transaction AdvancedVanishTable.select { AdvancedVanishTable.uuid eq key.toString() }
                    .singleOrNull()?.getOrNull(AdvancedVanishTable.state) == true
            }
        } catch (e: Exception) {
            AdvancedVanish.log(Level.SEVERE, "There was an error while attempting to make a connection with SQL: ")
            e.printStackTrace()
        }

        return false
    }

    override fun setAsync(key: UUID, value: Boolean) {
        Bukkit.getScheduler().runTaskAsynchronously(AdvancedVanish.instance!!, Runnable {
            try {
                transaction {
                    AdvancedVanishTable.select { AdvancedVanishTable.uuid eq key.toString() }.singleOrNull()?.getOrNull(AdvancedVanishTable.id)?.let { id ->
                        AdvancedVanishTable.update ({ AdvancedVanishTable.id eq id }) {
                            it[state] = value
                        }
                    } ?: run {
                        AdvancedVanishTable.insert {
                            it[uuid] = key.toString()
                            it[state] = value
                        }
                    }
                }
            } catch (e: Exception) {
                AdvancedVanish.log(Level.SEVERE, "There was an error while attempting to make a connection with SQL: ")
                e.printStackTrace()
            }
        })
    }
}