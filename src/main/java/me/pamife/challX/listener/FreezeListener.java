package me.pamife.challX.listener;

import me.pamife.challX.ChallX;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FreezeListener implements Listener {

    private boolean isFrozen() {
        return !ChallX.getInstance().getTimerManager().isRunning();
    }

    private boolean isPlayerExcluded(Player player) {
        return ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isFrozen() && !isPlayerExcluded(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isFrozen() && !isPlayerExcluded(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (isFrozen()) {
            if (event.getEntity() instanceof Player player) {
                if (isPlayerExcluded(player)) return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (isFrozen()) {
            if (event.getDamager() instanceof Player player) {
                if (isPlayerExcluded(player)) return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isFrozen() && !isPlayerExcluded(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isFrozen() && !isPlayerExcluded(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (isFrozen()) {
            if (event.getEntity() instanceof Player player) {
                if (isPlayerExcluded(player)) return;
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (isFrozen() && event.getWhoClicked() instanceof Player player) {
            if (!isPlayerExcluded(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (isFrozen() && event.getEntity() instanceof Player player) {
            if (!isPlayerExcluded(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (isFrozen()) {
            if (event.getTarget() instanceof Player player) {
                if (isPlayerExcluded(player)) return;
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (isFrozen()) {
            if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
                event.setCancelled(true);
            }
        }
    }
}
