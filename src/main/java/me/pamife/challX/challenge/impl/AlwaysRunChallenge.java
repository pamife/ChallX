package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AlwaysRunChallenge extends BaseChallenge {

    private BukkitTask task;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Integer> stillTicks = new HashMap<>();

    @Override
    public String getName() {
        return "Immer Laufen";
    }

    @Override
    public String getDescription() {
        return "Die Spieler können nie still stehen. Stillstehen fügt Schaden zu.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a§lImmer Laufen");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        lastLocations.clear();
        stillTicks.clear();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                    if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) continue;

                    UUID uuid = player.getUniqueId();
                    Location currentLoc = player.getLocation();
                    Location lastLoc = lastLocations.get(uuid);

                    if (lastLoc != null) {
                        // Position vergleichen (X, Y, Z abgerundet auf 2 Dezimalstellen zur Vermeidung kleiner Floating-Point-Fehler)
                        boolean moved = Math.abs(currentLoc.getX() - lastLoc.getX()) > 0.05 ||
                                        Math.abs(currentLoc.getY() - lastLoc.getY()) > 0.05 ||
                                        Math.abs(currentLoc.getZ() - lastLoc.getZ()) > 0.05;

                        if (!moved) {
                            int ticks = stillTicks.getOrDefault(uuid, 0) + 1;
                            stillTicks.put(uuid, ticks);

                            if (ticks >= 3) { // 3 Intervalle * 10 Ticks = 30 Ticks = 1.5 Sekunden
                                player.damage(2.0); // 1 Herz Schaden
                                player.sendMessage("§cBleib nicht stehen! Du musst laufen!");
                            }
                        } else {
                            stillTicks.put(uuid, 0);
                        }
                    }

                    lastLocations.put(uuid, currentLoc.clone());
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 10L, 10L); // Check alle 0.5 Sekunden (10 Ticks)
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        lastLocations.clear();
        stillTicks.clear();
    }
}
