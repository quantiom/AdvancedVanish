package me.quantiom.advancedvanish.listener

import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.util.AdvancedVanishAPI
import me.quantiom.advancedvanish.util.isVanished
import me.quantiom.advancedvanish.util.sendConfigMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Chest
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

        if (player.hasPermission(vanishPermission) && Config.getValueOrDefault("vanish-on-join", false)) {
            AdvancedVanishAPI.vanishPlayer(player, true)
            player.sendConfigMessage("vanish-on")
        }

        if (!player.hasPermission(vanishPermission)) {
            AdvancedVanishAPI.refreshVanished(player)
        }

        if (!Config.getValueOrDefault("when-vanished.join-messages", false)) {
            event.joinMessage = null
        }
    }

    @EventHandler
    private fun onDisconnect(event: PlayerQuitEvent) {
        if (event.player.isVanished()) {
            AdvancedVanishAPI.unVanishPlayer(event.player, true)
        }

        if (!Config.getValueOrDefault("when-vanished.leave-messages", false)) {
            event.quitMessage = null
        }
    }

    @EventHandler
    private fun onChat(event: AsyncPlayerChatEvent) =
        genericEventCancel(
            event,
            event.player,
            "when-vanished.send-messages",
            "cannot-chat-while-vanished"
        )

    @EventHandler
    private fun onBlockPlace(event: BlockPlaceEvent) =
        genericEventCancel(
            event,
            event.player,
            "when-vanished.place-blocks",
            "cannot-place-blocks-while-vanished"
        )

    @EventHandler
    private fun onBlockBreak(event: BlockBreakEvent) =
        genericEventCancel(
            event,
            event.player,
            "when-vanished.break-blocks",
            "cannot-break-blocks-while-vanished"
        )

    @EventHandler
    private fun onInteract(event: PlayerInteractEvent) {
        if (event.player.isVanished() && (event.clickedBlock?.type == Material.CHEST || event.clickedBlock?.type == Material.TRAPPED_CHEST) && event.action == Action.RIGHT_CLICK_BLOCK) {
            if (!Config.getValueOrDefault(
                    "when-vanished.open-and-use-chests",
                    false
                )) {
                val chestInventory = (event.clickedBlock.state as Chest).inventory
                val cloneInventory = Bukkit.createInventory(null, chestInventory.size, "AdvancedVanish Chest").also { it.contents = chestInventory.contents }

                event.player.openInventory(cloneInventory)
                event.player.sendConfigMessage("opening-chest-silently")
                event.isCancelled = true
            }
        } else if (!event.hasBlock()) genericEventCancel(
            event,
            event.player,
            "when-vanished.interact",
            ""
        )
    }

    @EventHandler
    private fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory.title == "AdvancedVanish Chest") event.isCancelled = true
    }

    @EventHandler
    private fun onPickUp(event: PlayerPickupItemEvent) =
        genericEventCancel(
            event,
            event.player,
            "when-vanished.pick-up-items",
            ""
        )

    @EventHandler
    private fun onDrop(event: PlayerDropItemEvent) =
        genericEventCancel(
            event,
            event.player,
            "when-vanished.drop-items",
            "cannot-drop-items-while-vanished"
        )

    @EventHandler
    private fun onHungerLoss(event: FoodLevelChangeEvent) {
        if (event.entity is Player) {
            genericEventCancel(
                event,
                event.entity as Player,
                "when-vanished.lose-hunger",
                ""
            )
        }
    }

    @EventHandler
    private fun onEntityTarget(event: EntityTargetLivingEntityEvent) {
        if (event.target is Player) {
            genericEventCancel(
                event,
                event.entity as Player,
                "when-vanished.mob-targeting",
                ""
            )
        }
    }

    @EventHandler
    private fun onDamage(event: EntityDamageByEntityEvent) {
        (event.damager as? Player)?.let { damager ->
            if (damager.isVanished() && !Config.getValueOrDefault("when-vanished.attack-entities", false)) {
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

    private fun genericEventCancel(event: Cancellable, player: Player, toggle: String, message: String) {
        if (player.isVanished() && !Config.getValueOrDefault(toggle, false)) {
            if (message.isNotEmpty()) player.sendConfigMessage(message)
            event.isCancelled = true
        }
    }
}