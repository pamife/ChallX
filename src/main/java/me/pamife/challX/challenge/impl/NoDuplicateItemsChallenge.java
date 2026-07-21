package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;

public class NoDuplicateItemsChallenge extends BaseChallenge {

    private BukkitTask task;

    @Override
    public String getName() {
        return "Keine doppelten Items";
    }

    @Override
    public String getDescription() {
        return "Spieler dürfen keine zwei Slots mit demselben Item-Typ im Inventar haben.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eKeine doppelten Items");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        // Kontinuierliche Prüfung jede Sekunde (falls Spieler Items im Inventar belassen)
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkPlayer(player);
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L);
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void checkPlayer(Player player) {
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        if (player.isDead()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (hasDuplicates(player)) {
            player.damage(2.0); // 1 Herz Schaden
            player.sendMessage("§cDu hast doppelte Items im Inventar!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        }
    }

    private boolean hasDuplicates(Player player) {
        Set<Material> seen = new HashSet<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            Material type = item.getType();
            if (type == Material.AIR) continue;
            if (item.getAmount() <= 0) continue; // Skip leere Ghost-Items

            if (seen.contains(type)) {
                return true; // Doppelter Item-Typ gefunden!
            }
            seen.add(type);
        }
        return false;
    }

    // --- Events für unmittelbare Reaktion ---
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (event.getWhoClicked() instanceof Player player) {
            // 1 Tick verzögern, damit Spigot das Inventar aktualisiert hat
            Bukkit.getScheduler().runTask(ChallX.getInstance(), () -> checkPlayer(player));
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!isEnabled()) return;
        if (event.getEntity() instanceof Player player) {
            Bukkit.getScheduler().runTask(ChallX.getInstance(), () -> checkPlayer(player));
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!isEnabled()) return;
        Bukkit.getScheduler().runTask(ChallX.getInstance(), () -> checkPlayer(event.getPlayer()));
    }
}
