package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
                    if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                    if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;
                    if (player.isDead()) continue;

                    if (hasDuplicates(player)) {
                        player.damage(2.0); // 1 Herz Schaden
                        player.sendMessage("§cDu hast doppelte Items im Inventar!");
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L); // Jede Sekunde prüfen
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private boolean hasDuplicates(Player player) {
        Set<Material> seen = new HashSet<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            Material type = item.getType();
            if (seen.contains(type)) {
                return true; // Doppelter Item-Typ gefunden!
            }
            seen.add(type);
        }
        return false;
    }
}
