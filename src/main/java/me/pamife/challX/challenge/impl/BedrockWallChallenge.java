package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class BedrockWallChallenge extends BaseChallenge {

    private BukkitTask task;
    private double wallZ = 0.0;
    private boolean initialized = false;

    @Override
    public String getName() {
        return "Bedrock-Wand";
    }

    @Override
    public String getDescription() {
        return "Eine sich bewegende Bedrock-Wand verfolgt alle Spieler (+Z-Richtung).";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.BEDROCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§8§lBedrock-Wand");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        initialized = false;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                Player target = null;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
                    if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;
                    target = p;
                    break;
                }

                if (target == null) return;

                if (!initialized) {
                    wallZ = target.getLocation().getZ() - 20.0;
                    initialized = true;
                    Bukkit.broadcastMessage("§8[Bedrock-Wand] §eDie Wand hat sich bei Z = " + (int)wallZ + " formiert!");
                }

                // Wand bewegt sich 1 Block vorwärts (+Z Richtung)
                int oldWallZ = (int) wallZ;
                wallZ += 1.0;
                int newWallZ = (int) wallZ;

                // Alte Wand entfernen (mit Luft ersetzen)
                for (int x = target.getLocation().getBlockX() - 12; x <= target.getLocation().getBlockX() + 12; x++) {
                    for (int y = target.getLocation().getBlockY() - 5; y <= target.getLocation().getBlockY() + 15; y++) {
                        Block block = target.getWorld().getBlockAt(x, y, oldWallZ);
                        if (block.getType() == Material.BEDROCK) {
                            block.setType(Material.AIR);
                        }
                    }
                }

                // Neue Wand platzieren
                for (int x = target.getLocation().getBlockX() - 12; x <= target.getLocation().getBlockX() + 12; x++) {
                    for (int y = target.getLocation().getBlockY() - 5; y <= target.getLocation().getBlockY() + 15; y++) {
                        Block block = target.getWorld().getBlockAt(x, y, newWallZ);
                        if (block.getType() == Material.AIR || block.getType().isBurnable() || block.getType() == Material.GRASS_BLOCK || block.getType() == Material.DIRT || block.getType() == Material.STONE) {
                            block.setType(Material.BEDROCK);
                        }
                    }
                }

                // Spieler-Kollision prüfen
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
                    if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;

                    if (p.getLocation().getZ() <= wallZ) {
                        p.damage(20.0); // Sofortiger Tod
                        p.sendMessage("§cDu wurdest von der Bedrock-Wand zerquetscht!");
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 60L, 60L); // Alle 3 Sekunden (60 Ticks)
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
