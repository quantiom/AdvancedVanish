package me.quantiom.advancedvanish.state

import com.google.common.collect.Maps
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.util.AdvancedVanishAPI
import me.quantiom.advancedvanish.util.isVanished
import org.bukkit.Bukkit
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.NullPointerException
import java.lang.reflect.Type
import java.util.*

object VanishStateManager {
    val savedVanishStates: MutableMap<UUID, Boolean> = Maps.newHashMap()

    fun onConfigReload() {
        if (!Config.getValueOrDefault("keep-vanish-state", false)
            || !Config.getValueOrDefault("keep-vanish-state-persistent", false)) {
            return
        }

        File(AdvancedVanish.instance!!.dataFolder.path).also {
            if (!it.exists()) it.mkdirs()
        }

        this.load()
    }

    fun onDisable() {
        if (!Config.getValueOrDefault("keep-vanish-state", false)
            || !Config.getValueOrDefault("keep-vanish-state-persistent", false)) {
            return
        }

        Bukkit.getOnlinePlayers().forEach {
            val isVanished = it.isVanished()

            if (isVanished || it.hasPermission(Config.getValueOrDefault("permissions.vanish", "advancedvanish.vanish"))) {
                this.savedVanishStates[it.uniqueId] = isVanished
            }
        }

        this.save()
    }

    private fun getFile(): File? {
        try {
            return File(
                "${AdvancedVanish.instance!!.dataFolder}${File.separator}vanishStates.json"
            ).also {
                if (!it.exists()) {
                    it.createNewFile()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun save() {
        try {
            this.getFile()?.let {
                val jsonElement: String = Gson().toJson(this.savedVanishStates)
                FileWriter(it, false).close()
                val fileWriter = FileWriter(it)
                fileWriter.write(jsonElement)
                fileWriter.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun load() {
        try {
            this.getFile()?.let {
                val type: Type = object : TypeToken<MutableMap<UUID, Boolean>>() {}.type
                val fileReader = FileReader(it)
                val inventories: MutableMap<UUID, Boolean> = Gson().fromJson(fileReader, type)
                this.savedVanishStates.putAll(inventories)
                fileReader.close()
            }
        } catch (ignored: NullPointerException) {

        }
    }
}