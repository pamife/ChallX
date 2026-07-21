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
        if (!isEnabled()) return;
        
        boolean timerRunning = ChallX.getInstance().getTimerManager().isRunning();
        boolean excluded = ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId());
        boolean creativeOrSpec = player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE;

        boolean duplicates = hasDuplicates(player);

        if (duplicates) {
            if (!timerRunning) {
                // Timer läuft nicht -> Keine Aktion
                return;
            }
            if (creativeOrSpec) {
                player.sendMessage("§e[Debug] Duplikat erkannt! Aber kein Schaden wegen Creative/Spectator-Modus.");
                return;
            }
            if (excluded) {
                player.sendMessage("§e[Debug] Duplikat erkannt! Aber kein Schaden, weil du ausgeschlossen bist.");
                return;
            }

            // Normaler Schaden
            player.damage(2.0); // 1 Herz Schaden
            player.sendMessage("§cDu hast doppelte Items im Inventar!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        }
    }

    private boolean hasDuplicates(Player player) {
        Set<Material> seen = new HashSet<>();
        
        // Debugging-Ausgabe an die Konsole zur Nachvollziehbarkeit
        Bukkit.getLogger().info("[ChallX] Prüfe Inventar von " + player.getName());

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            Material type = item.getType();
            if (type == Material.AIR) continue;
            if (item.getAmount() <= 0) continue;

            Bukkit.getLogger().info("[ChallX]   Slot enthält: " + type + " (Menge: " + item.getAmount() + ")");

            if (seen.contains(type)) {
                Bukkit.getLogger().info("[ChallX]   -> DUPLIKAT GEFUNDEN: " + type);
                return true;
            }
            seen.add(type);
        }
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (event.getWhoClicked() instanceof Player player) {
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
