package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WalkingDamageChallenge extends BaseChallenge {

    private final Map<UUID, Double> distanceAccumulator = new HashMap<>();

    @Override
    public String getName() {
        return "Laufen = Schaden";
    }

    @Override
    public String getDescription() {
        return "Jeder gelaufene Block fügt dem Spieler 1 Herz Schaden zu.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cLaufen = Schaden");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onDisable() {
        distanceAccumulator.clear();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (ChallX.getInstance().getSettingsManager().isExcluded(uuid)) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        // Teleportationen oder Spawns ignorieren (Distanz > 8 Blöcke in einem Tick)
        if (distance > 0.0 && distance < 8.0) {
            double accumulated = distanceAccumulator.getOrDefault(uuid, 0.0);
            accumulated += distance;

            if (accumulated >= 1.0) {
                int blocks = (int) accumulated;
                accumulated -= blocks;

                // 2.0 HP (1 Herz) Schaden pro Block
                double damage = blocks * 2.0;
                player.damage(damage);
                player.sendMessage("§cDu bist gelaufen und hast §4" + blocks + " Herz(en) §cSchaden erhalten!");
            }

            distanceAccumulator.put(uuid, accumulated);
        }
    }
}
