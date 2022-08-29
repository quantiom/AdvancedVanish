package me.quantiom.advancedvanish.listener

import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.redis.RedisManager
import me.quantiom.advancedvanish.state.VanishStateManager
import me.quantiom.advancedvanish.util.AdvancedVanishAPI
import me.quantiom.advancedvanish.util.isVanished
import me.quantiom.advancedvanish.util.sendConfigMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Barrel
import org.bukkit.block.Chest
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*

object VanishListener : Listener {
    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        val vanishPermission = Config.getValueOrDefault(
            "permissions.vanish",
            "advancedvanish.vanish"
        )

        val joinVanishedPermission = Config.getValueOrDefault(
            "permissions.join-vanished",
            "advancedvanish.join-vanished"
        )

        var doVanish = false

        if (player.hasPermission(vanishPermission)) {
            if (RedisManager.proxySupportEnabled && RedisManager.loginVanishStates.containsKey(player.uniqueId)) {
                doVanish = RedisManager.loginVanishStates[player.uniqueId]!!
            } else {
                if (Config.getValueOrDefault("keep-vanish-state", false) && VanishStateManager.savedVanishStates.containsKey(player.uniqueId)) {
                    if (VanishStateManager.savedVanishStates[player.uniqueId]!!) {
                        doVanish = true
                        VanishStateManager.savedVanishStates.remove(player.uniqueId)
                    }
                } else if (Config.getValueOrDefault("vanish-on-join", false) || player.hasPermission(joinVanishedPermission)) {
                    doVanish = true
                }
            }
        }

        if (doVanish) {
            AdvancedVanishAPI.vanishPlayer(player, true)
            player.sendConfigMessage("vanish-on")
        }

        AdvancedVanishAPI.refreshVanished(player)

        if (!Config.getValueOrDefault("when-vanished.join-messages", false)) {
            if (AdvancedVanishAPI.isPlayerVanished(player)) {
                event.joinMessage = null
            }
        }
    }

    @EventHandler
    private fun onDisconnect(event: PlayerQuitEvent) {
        val player = event.player
        val isVanished = player.isVanished()

        if (isVanished || player.hasPermission(Config.getValueOrDefault("permissions.vanish", "advancedvanish.vanish"))) {
            VanishStateManager.savedVanishStates[player.uniqueId] = isVanished

            if (isVanished) {
                AdvancedVanishAPI.unVanishPlayer(player, true)
            }
        }

        if (!Config.getValueOrDefault("when-vanished.leave-messages", false)) {
            if (AdvancedVanishAPI.isPlayerVanished(player)) {
                event.quitMessage = null
            }
        }
    }

    @EventHandler
    private fun onChat(event: AsyncPlayerChatEvent) =
        genericEventCancel(
            event,
            event.player,
            "when-vanished.send-messages",
            "cannot-chat-while-vanished",
            false
        )

    @EventHandler
    private fun onBlockPlace(event: BlockPlaceEvent) =
        genericEventCancel(
            event,
            event.player,
            "when-vanished.place-blocks",
            "cannot-place-blocks-while-vanished",
            true
        )

    @EventHandler
    private fun onBlockBreak(event: BlockBreakEvent) =
        genericEventCancel(
            event,
            event.player,
            "when-vanished.break-blocks",
            "cannot-break-blocks-while-vanished",
            true
        )

    @EventHandler
    private fun onInteract(event: PlayerInteractEvent) {
        if (event.player.isVanished() && event.action == Action.RIGHT_CLICK_BLOCK) {
            if (event.clickedBlock?.type == Material.CHEST || event.clickedBlock?.type == Material.TRAPPED_CHEST || event.clickedBlock?.type == Material.ENDER_CHEST || event.clickedBlock?.type!!.name.endsWith("SHULKER_BOX") || event.clickedBlock?.type!!.name == "BARREL") {
                if (!Config.getValueOrDefault(
                        "when-vanished.open-and-use-chests",
                        false
                    )
                ) {
                    val inventoryName: String
                    val inventory = when (event.clickedBlock!!.type.name) {
                        "CHEST", "TRAPPED_CHEST" -> {
                            inventoryName = "Chest"
                            (event.clickedBlock?.state as Chest).inventory
                        }
                        "BARREL" -> {
                            inventoryName = "Barrel"
                            (event.clickedBlock?.state as Barrel).inventory
                        }
                        "ENDER_CHEST" -> {
                            inventoryName = "Enderchest"
                            event.player.enderChest
                        }
                        else -> {
                            inventoryName = "Shulker"
                            (event.clickedBlock?.state as ShulkerBox).inventory
                        }
                    }

                    if (inventoryName == "Enderchest") {
                        event.player.openInventory(inventory)
                    } else {
                        val cloneInventory =
                            Bukkit.createInventory(null, inventory.size, "AdvancedVanish $inventoryName")
                                .also { it.contents = inventory.contents }

                        event.player.openInventory(cloneInventory)
                    }

                    event.player.sendConfigMessage("opening-container-silently", "%type%" to inventoryName.lowercase())
                    event.isCancelled = true
                }
            }
        } else if (!VanishStateManager.canInteract(event.player)) {
            genericEventCancel(
                event,
                event.player,
                "when-vanished.interact",
                "",
                true
            )
        }
    }

    @EventHandler
    private fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title.startsWith("AdvancedVanish ")) event.isCancelled = true
    }

    @EventHandler
    private fun onPickUp(event: PlayerPickupItemEvent) =
        genericEventCancel(
            event,
            event.player,
            "when-vanished.pick-up-items",
            "",
            true
        )

    @EventHandler
    private fun onDrop(event: PlayerDropItemEvent) =
        genericEventCancel(
            event,
            event.player,
            "when-vanished.drop-items",
            "cannot-drop-items-while-vanished",
            true
        )

    @EventHandler
    private fun onHungerLoss(event: FoodLevelChangeEvent) {
        if (event.entity is Player) {
            genericEventCancel(
                event,
                event.entity as Player,
                "when-vanished.lose-hunger",
                "",
                false
            )
        }
    }

    @EventHandler
    private fun onEntityTarget(event: EntityTargetLivingEntityEvent) {
        if (event.target is Player) {
            genericEventCancel(
                event,
                event.target as Player,
                "when-vanished.mob-targeting",
                "",
                false
            )
        }
    }

    @EventHandler
    private fun onDamage(event: EntityDamageByEntityEvent) {
        (event.damager as? Player)?.let { damager ->
            if (damager.isVanished() && !Config.getValueOrDefault("when-vanished.attack-entities", false) && !VanishStateManager.canInteract(damager)) {
                damager.sendConfigMessage("cannot-attack-entities-while-vanished")
                event.isCancelled = true
            }
        }

        (event.entity as? Player)?.let { attacked ->
            if (attacked.isVanished() && !Config.getValueOrDefault("when-vanished.receive-damage-from-entities", false)) {
                event.isCancelled = true
            }
        }
    }

    private fun genericEventCancel(event: Cancellable, player: Player, toggle: String, message: String, ignoreIfCanInteract: Boolean) {
        if (player.isVanished() && !Config.getValueOrDefault(toggle, false)) {
            if (ignoreIfCanInteract && VanishStateManager.canInteract(player)) {
                return
            }

            if (message.isNotEmpty()) player.sendConfigMessage(message)
            event.isCancelled = true
        }
    }
}
