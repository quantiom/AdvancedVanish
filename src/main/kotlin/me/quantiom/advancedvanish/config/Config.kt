package me.quantiom.advancedvanish.config

import com.google.common.collect.Maps
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.util.applyPlaceholders
import me.quantiom.advancedvanish.util.color
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import java.io.File
import java.util.logging.Level


object Config {
    var savedConfig: FileConfiguration? = null
    var usingPriorities = false

    private const val CONFIG_VERSION = 1
    private var messages: MutableMap<String, List<String>> = Maps.newHashMap()

    fun reload() {
        if (!File(AdvancedVanish.instance!!.dataFolder.toString() + File.separator, "config.yml").exists()) {
            AdvancedVanish.instance!!.saveDefaultConfig()
        }

        AdvancedVanish.instance!!.let {
            it.reloadConfig()
            this.savedConfig = it.config
        }

        // config-version check
        if (this.getValueOrDefault("config-version", CONFIG_VERSION) != CONFIG_VERSION) {
            File(AdvancedVanish.instance!!.dataFolder.toString() + File.separator, "config.yml").also {
                if (it.exists()) {
                    val backupFile = File(AdvancedVanish.instance!!.dataFolder.toString() + File.separator, "config-backup-${System.currentTimeMillis()}.yml")
                    it.copyTo(backupFile)
                    it.delete()
                    AdvancedVanish.instance!!.saveDefaultConfig()
                }
            }

            AdvancedVanish.log(Level.WARNING, "WARNING: Config version mismatch, a backup of the old config has been made and the current config has been replaced.")
            this.reload()
            return
        }

        this.reloadMessages()
        this.usingPriorities = this.getValueOrDefault("priority.enable", false)
    }

    inline fun <reified T> getValue(key: String): T? {
        val value = this.savedConfig!!.get(key)

        if (value is T) {
            return value
        }

        // some default values
        return when (T::class) {
            Boolean::class -> false as T
            String::class -> "" as T
            Int::class, Double::class -> 0 as T
            else -> null
        }
    }

    inline fun <reified T> getValueOrDefault(key: String, default: T): T {
        val value = this.savedConfig!!.get(key)

        if (value is T) {
            return value
        }

        return default
    }

    private fun getMessage(key: String): List<String> {
        return messages[key] ?: listOf("&7Message not found for &c$key&7.".color())
    }

    private fun getMessage(key: String, vararg pairs: Pair<String, String>): List<String> {
        return messages[key]?.applyPlaceholders(*pairs) ?: listOf("&7Message not found for &c$key&7.".color())
    }

    fun sendMessage(player: CommandSender, key: String) {
        var prefix = ""

        if (this.getValueOrDefault("messages.prefix.enabled", false)) {
            prefix = this.getValueOrDefault("messages.prefix.value", "&c[AdvancedVanish]&f ").color()
        }

        this.getMessage(key).filter { it.isNotEmpty() }.forEach { player.sendMessage(prefix + it) }
    }

    fun sendMessage(player: CommandSender, key: String, vararg pairs: Pair<String, String>) {
        var prefix = ""

        if (this.getValueOrDefault("messages.prefix.enabled", false)) {
            prefix = this.getValueOrDefault("messages.prefix.value", "&c[AdvancedVanish]&f ").color()
        }

        this.getMessage(key, *pairs).filter { it.isNotEmpty() }.forEach { player.sendMessage(prefix + it) }
    }

    private fun reloadMessages() {
        messages.clear()

        this.savedConfig?.getConfigurationSection("messages")?.let {
            it.getKeys(false).forEach { key ->
                if (it.isString(key)) {
                    messages[key] = listOf(it.getString(key)).map(String::color)
                } else if (it.isList(key)) {
                    messages[key] = it.getList(key).filterIsInstance<String>().map(String::color)
                }
            }
        }
    }
}