package me.quantiom.advancedvanish.sync.impl

import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.sync.IServerSyncStore
import org.bukkit.Bukkit
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import java.util.logging.Level

object SqlServerSyncStore : IServerSyncStore {
    private var connection: Connection? = null

    override fun setup(): Boolean {
        val host = Config.getValueOrDefault("cross-server-support.sql.ip", "127.0.0.1")
        val port = Config.getValueOrDefault("cross-server-support.sql.port", 3306)
        val username = Config.getValueOrDefault("cross-server-support.sql.username", "root")
        val password = Config.getValueOrDefault("cross-server-support.sql.password", "")
        val database = Config.getValueOrDefault("cross-server-support.sql.database", "minecraft")

        try {
            this.connection = DriverManager.getConnection(
                "jdbc:mysql://$host:$port/$database",
                username,
                password
            )

            val statement = this.connection!!.createStatement()
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS advancedvanish (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    uuid CHAR(36) NOT NULL,
                    state BOOLEAN NOT NULL
                );
            """.trimIndent())

        } catch (e: SQLException) {
            AdvancedVanish.log(Level.SEVERE, "There was an error while attempting to make a connection with SQL: ")
            e.printStackTrace()
            return false
        }

        AdvancedVanish.log(Level.INFO, "Successfully connected to the SQL database.")
        return true
    }

    override fun close() {
        this.connection?.close()
    }

    override fun get(key: UUID): Boolean {
        val query = "SELECT state FROM advancedvanish WHERE uuid = ?"
        try {
            val preparedStatement = this.connection!!.prepareStatement(query)
            preparedStatement.setString(1, key.toString())
            val resultSet = preparedStatement.executeQuery()

            if (resultSet.next()) {
                return resultSet.getBoolean("state")
            }
        } catch (e: SQLException) {
            AdvancedVanish.log(Level.SEVERE, "There was an error while attempting to make a connection with SQL: ")
            e.printStackTrace()
        }

        return false
    }

    override fun setAsync(key: UUID, value: Boolean) {
        Bukkit.getScheduler().runTaskAsynchronously(AdvancedVanish.instance!!, Runnable {
            try {
                val updateQuery = "INSERT INTO advancedvanish (uuid, state) VALUES (?, ?) ON DUPLICATE KEY UPDATE state = ?"
                val preparedStatement = connection!!.prepareStatement(updateQuery)
                preparedStatement.setString(1, key.toString())
                preparedStatement.setBoolean(2, value)
                preparedStatement.setBoolean(3, value)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                AdvancedVanish.log(Level.SEVERE, "There was an error while attempting to make a connection with SQL: ")
                e.printStackTrace()
            }
        })
    }
}