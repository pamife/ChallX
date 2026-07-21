package me.pamife.challX.listener;

import me.pamife.challX.ChallX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FreezeListener implements Listener {

    private final Map<UUID, Long> lastNotify = new HashMap<>();

    private boolean isFrozen() {
        return !ChallX.getInstance().getTimerManager().isRunning();
    }

    private boolean isPlayerExcluded(Player player) {
        return ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId());
    }

    private void notifyPaused(Player player) {
        long now = System.currentTimeMillis();
        long last = lastNotify.getOrDefault(player.getUniqueId(), 0L);
        if (now - last > 3000) { // 3 Sekunden Cooldown gegen Chat-Spam
            lastNotify.put(player.getUniqueId(), now);

            Component message = Component.text("§cDer Timer ist pausiert! ")
                    .append(Component.text("§a§l[Jetzt starten]")
                            .clickEvent(ClickEvent.runCommand("/timer resume"))
                            .hoverEvent(HoverEvent.showText(Component.text("§7Klicke, um den Timer fortzusetzen"))))
                    .append(Component.text(" §7um die Challenge fortzusetzen."));

            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isFrozen() && !isPlayerExcluded(event.getPlayer())) {
            event.setCancelled(true);
            notifyPaused(event.getPlayer());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isFrozen() && !isPlayerExcluded(event.getPlayer())) {
            event.setCancelled(true);
            notifyPaused(event.getPlayer());
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
                notifyPaused(player);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isFrozen() && !isPlayerExcluded(event.getPlayer())) {
            event.setCancelled(true);
            notifyPaused(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isFrozen() && !isPlayerExcluded(event.getPlayer())) {
            event.setCancelled(true);
            notifyPaused(event.getPlayer());
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (isFrozen()) {
            if (event.getEntity() instanceof Player player) {
                if (isPlayerExcluded(player)) return;
                event.setCancelled(true);
                notifyPaused(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (isFrozen() && event.getWhoClicked() instanceof Player player) {
            if (!isPlayerExcluded(player)) {
                event.setCancelled(true);
                notifyPaused(player);
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
