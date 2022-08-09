package me.quantiom.advancedvanish.util

import me.quantiom.advancedvanish.AdvancedVanish
import org.bukkit.Bukkit
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.function.Consumer

// credit: https://www.spigotmc.org/wiki/creating-an-update-checker-that-checks-for-updates/

object UpdateChecker {
    private const val RESOURCE_ID = 86036

    fun getVersion(consumer: Consumer<String?>) {
        Bukkit.getScheduler().runTaskAsynchronously(AdvancedVanish.instance!!, Runnable {
            try {
                URL("https://api.spigotmc.org/legacy/update.php?resource=${RESOURCE_ID}").openStream()
                    .use { inputStream ->
                        Scanner(inputStream).use { scanner ->
                            if (scanner.hasNext()) {
                                consumer.accept(scanner.next())
                            }
                        }
                    }
            } catch (exception: IOException) {
                AdvancedVanish.instance!!.logger.info("Unable to check for updates: " + exception.message)
            }
        })
    }
}