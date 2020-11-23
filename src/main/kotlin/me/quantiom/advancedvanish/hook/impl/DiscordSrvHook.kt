package me.quantiom.advancedvanish.hook.impl

import me.quantiom.advancedvanish.hook.IHook

// Implementation is in VanishUtil#vanishPlayer and VanishUtil#unVanishPlayer
class DiscordSrvHook : IHook {
    override fun getID() = "DiscordSrv"
}