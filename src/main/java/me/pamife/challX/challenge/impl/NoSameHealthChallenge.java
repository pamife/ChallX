package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoSameHealthChallenge extends BaseChallenge {

    private BukkitTask task;

    @Override
    public String getName() {
        return "Keine gleichen Herzen";
    }

    @Override
    public String getDescription() {
        return "Wenn zwei Spieler gleich viele Herzen haben, sterben sie augenblicklich.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.REDSTONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lKeine gleichen Herzen");
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

                List<Player> activePlayers = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
                    if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;
                    activePlayers.add(p);
                }

                if (activePlayers.size() < 2) return;

                Map<Long, List<Player>> healthGroups = new HashMap<>();
                for (Player p : activePlayers) {
                    // Halbe Herzen als gerundeter Long Key
                    long halfHearts = Math.round(p.getHealth());
                    healthGroups.computeIfAbsent(halfHearts, k -> new ArrayList<>()).add(p);
                }

                for (Map.Entry<Long, List<Player>> entry : healthGroups.entrySet()) {
                    if (entry.getValue().size() >= 2) {
                        for (Player p : entry.getValue()) {
                            p.damage(20.0);
                            p.sendMessage("§c[Keine gleichen Herzen] Du hattest genauso viele Herzen wie ein anderer Spieler!");
                        }
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L); // Check alle 1s
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
