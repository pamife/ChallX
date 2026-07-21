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
    private double wallX = 0.0;
    private double wallZ = 0.0;
    private double lastWallX = 0.0;
    private double lastWallZ = 0.0;
    private int lastY = 0;
    private boolean initialized = false;

    @Override
    public String getName() {
        return "Bedrock-Wand";
    }

    @Override
    public String getDescription() {
        return "Eine 1x1 Bedrock-Säule (25 Blöcke hoch) verfolgt dich und erstickt dich bei Kontakt.";
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

                Location pLoc = target.getLocation();

                if (!initialized) {
                    wallX = pLoc.getX() - 15.0;
                    wallZ = pLoc.getZ() - 15.0;
                    lastWallX = wallX;
                    lastWallZ = wallZ;
                    lastY = pLoc.getBlockY();
                    initialized = true;
                    Bukkit.broadcastMessage("§8[Bedrock-Säule] §eDie Säule hat sich formiert und verfolgt dich!");
                }

                // 1. Alte Säule abbauen
                int oldX = (int) Math.floor(lastWallX);
                int oldZ = (int) Math.floor(lastWallZ);
                for (int y = lastY - 5; y < lastY + 20; y++) {
                    Block b = target.getWorld().getBlockAt(oldX, y, oldZ);
                    if (b.getType() == Material.BEDROCK) {
                        b.setType(Material.AIR);
                    }
                }

                // 2. Berechne Vektor zum Spieler
                double dx = pLoc.getX() - wallX;
                double dz = pLoc.getZ() - wallZ;
                double distance = Math.sqrt(dx * dx + dz * dz);

                if (distance > 0.1) {
                    // Bewegt sich 0.35 Blöcke pro halbe Sekunde (0.7m/s) auf den Spieler zu
                    double step = Math.min(0.35, distance);
                    wallX += (dx / distance) * step;
                    wallZ += (dz / distance) * step;
                }

                // 3. Neue Säule aufstellen
                int newX = (int) Math.floor(wallX);
                int newZ = (int) Math.floor(wallZ);
                int currentY = pLoc.getBlockY();

                for (int y = currentY - 5; y < currentY + 20; y++) {
                    Block b = target.getWorld().getBlockAt(newX, y, newZ);
                    // Überschreibe nur Luft, Blätter, Gras, Stein und zerstörbare Blöcke
                    if (b.getType() == Material.AIR || b.getType().isBurnable() || b.getType() == Material.GRASS_BLOCK || b.getType() == Material.DIRT || b.getType() == Material.STONE) {
                        b.setType(Material.BEDROCK);
                    }
                }

                // Speichere Positionen für den nächsten Tick
                lastWallX = wallX;
                lastWallZ = wallZ;
                lastY = currentY;
            }
        }.runTaskTimer(ChallX.getInstance(), 10L, 10L); // Check alle 10 Ticks (0.5s)
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        initialized = false;
    }
}
