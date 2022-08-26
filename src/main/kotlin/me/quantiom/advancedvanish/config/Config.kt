package me.quantiom.advancedvanish.config

import co.aikar.commands.Locales
import co.aikar.commands.MessageKeys
import co.aikar.locales.MessageKeyProvider
import com.google.common.collect.Maps
import com.google.common.io.Closeables
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.state.VanishStateManager
import me.quantiom.advancedvanish.util.applyPlaceholders
import me.quantiom.advancedvanish.util.color
import me.quantiom.advancedvanish.util.colorLegacy
import me.quantiom.advancedvanish.util.sendComponentMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.logging.Level

object Config {
    var savedConfig: FileConfiguration? = null
    var usingPriorities = false

    private var CONFIG_VERSION: Int? = null

    // get config version from maven variable
    init {
        val resource = this.javaClass.classLoader.getResourceAsStream("app.properties")
        val p = Properties()
        var inputStream: InputStream? = null

        try {
            inputStream = resource.buffered()
            p.load(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            AdvancedVanish.instance!!.logger.log(Level.SEVERE, "Unable to read app.properties! Shutting down...")
            AdvancedVanish.instance!!.pluginLoader.disablePlugin(AdvancedVanish.instance!!)
        } finally {
            Closeables.closeQuietly(inputStream)
            CONFIG_VERSION = p.getProperty("application.config.version").toInt()
        }
    }

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
        val currentConfigVersion = this.getValueOrDefault("config-version", CONFIG_VERSION)!!
        if (currentConfigVersion != CONFIG_VERSION) {
            File(AdvancedVanish.instance!!.dataFolder.toString() + File.separator, "config.yml").also {
                if (it.exists()) {
                    val backupFile = File(AdvancedVanish.instance!!.dataFolder.toString() + File.separator, "config-backup-${System.currentTimeMillis()}.yml")
                    it.copyTo(backupFile)
                    it.delete()
                    AdvancedVanish.instance!!.saveDefaultConfig()

                    AdvancedVanish.instance!!.let { plugin ->
                        plugin.reloadConfig()

                        val oldYamlConfig = YamlConfiguration().also { config -> config.load(backupFile) }

                        for (key in oldYamlConfig.getKeys(false)) {
                            checkKeys(currentConfigVersion, CONFIG_VERSION!!, plugin.config, oldYamlConfig, key)
                        }

                        if (currentConfigVersion < 8) {
                            AdvancedVanish.log(Level.INFO, "Config messages have been updated to use the MiniMessage formatting. Please check your config.yml for more information.")
                        }

                        (plugin.config as YamlConfiguration).options().width(120)

                        plugin.saveConfig()
                    }
                }
            }

            AdvancedVanish.log(Level.INFO, "Config version mismatch (most likely because of an update), a backup of the old config has been made and the current config has been replaced.")
            this.reload()
            return
        }

        this.reloadMessages()
        this.reloadCommandHandlerMessages()
        VanishStateManager.onConfigReload()
        this.usingPriorities = this.getValueOrDefault("priority.enable", false)
    }

    private fun checkKeys(oldVersion: Int, newVersion: Int, newYamlConfig: FileConfiguration, oldYamlConfig: YamlConfiguration, currKey: String) {
        if (currKey == "config-version") return

        if (oldYamlConfig.isConfigurationSection(currKey)) {
            for (key in oldYamlConfig.getConfigurationSection(currKey)!!.getKeys(false)) {
                checkKeys(oldVersion, newVersion, newYamlConfig, oldYamlConfig, "${currKey}.${key}")
            }
        } else {
            if (newYamlConfig.contains(currKey) && oldYamlConfig.contains(currKey) && newYamlConfig.get(currKey)!!::class == oldYamlConfig.get(currKey)!!::class) {
                // update formatting to use MiniMessage
                if (newYamlConfig.get(currKey)!! is String && oldVersion < 8) {
                    newYamlConfig.set(currKey, MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(oldYamlConfig.get(currKey) as String)))
                } else {
                    newYamlConfig.set(currKey, oldYamlConfig.get(currKey))
                }
            }
        }
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
        return messages[key] ?: listOf("<gray>Message not found for <red>$key<gray>.")
    }

    private fun getMessage(key: String, vararg pairs: Pair<String, String>): List<String> {
        return messages[key]?.applyPlaceholders(*pairs) ?: listOf("<gray>Message not found for <red>$key<gray>.")
    }

    fun sendMessage(player: CommandSender, key: String) {
        var prefix = ""

        if (this.getValueOrDefault("messages.prefix.enabled", false)) {
            prefix = this.getValueOrDefault("messages.prefix.value", "<red>[AdvancedVanish]<white> ")
        }

        this.getMessage(key).filter { it.isNotEmpty() }.forEach { player.sendComponentMessage(prefix.color().append(it.color())) }
    }

    fun sendMessage(sender: CommandSender, key: String, vararg pairs: Pair<String, String>) {
        var prefix: Component = Component.text("")

        if (this.getValueOrDefault("messages.prefix.enabled", false)) {
            prefix = this.getValueOrDefault("messages.prefix.value", "<red>[AdvancedVanish]<white> ").color()
        }

        this.getMessage(key, *pairs).filter { it.isNotEmpty() }.forEach { sender.sendComponentMessage(prefix.append(it.color())) }
    }

    private fun reloadMessages() {
        messages.clear()

        this.savedConfig?.getConfigurationSection("messages")?.let {
            it.getKeys(false).forEach { key ->
                if (it.isString(key)) {
                    messages[key] = listOf(it.getString(key)!!)
                } else if (it.isList(key)) {
                    messages[key] = it.getList(key)!!.filterIsInstance<String>()
                }
            }
        }
    }

    private fun reloadCommandHandlerMessages() {
        val commandHandlerMessages: MutableMap<String, String> = Maps.newHashMap()

        this.savedConfig?.getConfigurationSection("command-handler-messages")?.let {
            it.getKeys(false).forEach { key ->
                if (it.isString(key)) {
                    commandHandlerMessages[key] = it.getString(key)!!
                }
            }
        }

        val prefix = if (this.savedConfig?.getConfigurationSection("command-handler-messages")?.getBoolean("use-prefix")!!) {
            this.getValueOrDefault("messages.prefix.value", "<red>[AdvancedVanish]<white> ")
        } else {
            ""
        }

        val getOrDefault: (String, String) -> String = { key, default ->
            prefix + if (commandHandlerMessages.containsKey(key)) commandHandlerMessages[key]!! else default
        }

        val messages: MutableMap<MessageKeyProvider, String> = Maps.newHashMap()

        messages[MessageKeys.UNKNOWN_COMMAND] = getOrDefault("unknown-command", "Invalid arguments.")
            .color().colorLegacy()

        messages[MessageKeys.INVALID_SYNTAX] = getOrDefault("invalid-syntax", "Usage: %command% %syntax%")
            .applyPlaceholders(
                "%command%" to "{command}",
                "%syntax%" to "{syntax}"
            )
            .color().colorLegacy()

        messages[MessageKeys.ERROR_PERFORMING_COMMAND] = getOrDefault("error-performing-command", "There was an error performing this command.")
            .color().colorLegacy()

        messages[MessageKeys.COULD_NOT_FIND_PLAYER] = getOrDefault("could-not-find-player", "Couldn't find a player by the name of <red>%search%<white>.")
            .applyPlaceholders("%search%" to "{search}")
            .color().colorLegacy()

        messages[MessageKeys.ERROR_PREFIX] = getOrDefault("generic-error", "Error: <red>%error%")
            .applyPlaceholders("%error%" to "{message}")
            .color().colorLegacy()

        AdvancedVanish.commandManager!!.locales.addMessages(Locales.ENGLISH, messages)
    }
}