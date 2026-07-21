package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class WaterMLGChallenge extends BaseChallenge {

    private BukkitTask task;
    private final Map<UUID, ItemStack> originalItems = new HashMap<>();
    private final List<Block> placedWater = new ArrayList<>();

    @Override
    public String getName() {
        return "Water MLG";
    }

    @Override
    public String getDescription() {
        return "Teleportiert alle Spieler alle 3 Minuten in eine zufällige Höhe für einen Water MLG.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.WATER_BUCKET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bWater MLG");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                Bukkit.broadcastMessage("§b[MLG] Mach dich bereit! Water MLG in 3 Sekunden...");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!isEnabled() || !ChallX.getInstance().getTimerManager().isRunning()) return;

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;

                            triggerMLG(player);
                        }
                    }
                }.runTaskLater(ChallX.getInstance(), 60L);
            }
        }.runTaskTimer(ChallX.getInstance(), 3600L, 3600L); // Alle 3 Minuten (3600 Ticks)
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        // Original-Items zurückgeben, falls noch jemand fliegt
        for (UUID uuid : originalItems.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                int slot = p.getInventory().getHeldItemSlot();
                p.getInventory().setItem(slot, originalItems.get(uuid));
            }
        }
        originalItems.clear();
        
        // Platziertes Wasser entfernen
        for (Block b : placedWater) {
            if (b.getType() == Material.WATER) {
                b.setType(Material.AIR);
            }
        }
        placedWater.clear();
    }

    private void triggerMLG(Player player) {
        UUID uuid = player.getUniqueId();
        int slot = player.getInventory().getHeldItemSlot();
        
        // Item im aktuellen Slot sichern
        ItemStack currentItem = player.getInventory().getItem(slot);
        originalItems.put(uuid, currentItem != null ? currentItem.clone() : null);

        // Wassereimer in die Hand legen
        player.getInventory().setItem(slot, new ItemStack(Material.WATER_BUCKET));

        // Teleportieren
        Location loc = player.getLocation();
        double height = 25 + new Random().nextInt(41); // 25 bis 65 Blöcke
        loc.setY(loc.getY() + height);
        player.teleport(loc);

        player.sendMessage("§b§lMLG! §eRette dich mit dem Wassereimer!");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (originalItems.containsKey(uuid)) {
            // Wenn der Spieler wieder auf dem Boden steht
            if (player.isOnGround()) {
                ItemStack original = originalItems.remove(uuid);
                int slot = player.getInventory().getHeldItemSlot();
                
                // Inventar aufräumen (Eimer entfernen)
                player.getInventory().remove(Material.WATER_BUCKET);
                player.getInventory().remove(Material.BUCKET);
                
                // Originales Item wiederherstellen
                player.getInventory().setItem(slot, original);
                player.sendMessage("§aMLG geschafft!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;

        if (originalItems.containsKey(player.getUniqueId())) {
            Block block = event.getBlock();
            placedWater.add(block);

            // Wasser nach 3 Sekunden entfernen
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (block.getType() == Material.WATER) {
                        block.setType(Material.AIR);
                    }
                    placedWater.remove(block);
                }
            }.runTaskLater(ChallX.getInstance(), 60L);
        }
    }
}
