package me.quantiom.advancedvanish.hook.impl

import dev.esophose.playerparticles.api.PlayerParticlesAPI
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.hook.IHook
import me.quantiom.advancedvanish.util.VanishUtil
import org.bukkit.scheduler.BukkitRunnable

class PlayerParticlesHook : IHook {
    override fun getID() = "PlayerParticles"

    private val updateTask: BukkitRunnable =
        object : BukkitRunnable() {
            override fun run() {
                VanishUtil.vanishedPlayers
                    .mapNotNull { PlayerParticlesAPI.getInstance().getPPlayer(it) }
                    .forEach { it.activeParticles.clear() }
            }
        }

    override fun onEnable() {
        this.updateTask.runTaskTimer(AdvancedVanish.instance, 0L, 20L)
    }

    override fun onDisable() {
        this.updateTask.cancel()
    }
}