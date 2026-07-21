package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AnvilRainChallenge extends BaseChallenge {

    private BukkitTask spawnTask;
    private BukkitTask collisionTask;
    private final Set<FallingBlock> activeAnvils = ConcurrentHashMap.newKeySet();

    @Override
    public String getName() {
        return "Amboss-Regen";
    }

    @Override
    public String getDescription() {
        return "Spawnt alle 3 Sekunden einen fallenden Amboss über jedem Spieler.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7Amboss-Regen");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        // Spawn Task (Alle 3 Sekunden)
        spawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                    if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;

                    // Amboss 15 Blöcke über dem Spieler spawnen
                    Location spawnLoc = player.getLocation().clone().add(0, 15, 0);
                    FallingBlock anvil = player.getWorld().spawnFallingBlock(spawnLoc, Bukkit.createBlockData(Material.ANVIL));
                    anvil.setHurtEntities(true);
                    activeAnvils.add(anvil);
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 60L, 60L);

        // Kollisions-Prüfungs Task (Jeden Tick)
        collisionTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isEnabled() || !ChallX.getInstance().getTimerManager().isRunning()) {
                    activeAnvils.clear();
                    return;
                }

                Iterator<FallingBlock> it = activeAnvils.iterator();
                while (it.hasNext()) {
                    FallingBlock anvil = it.next();

                    // Wenn der Amboss auf dem Boden liegt oder gelöscht wurde
                    if (!anvil.isValid() || anvil.isOnGround()) {
                        it.remove();
                        continue;
                    }

                    Location loc = anvil.getLocation();
                    for (Player player : loc.getWorld().getPlayers()) {
                        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;

                        Location pLoc = player.getLocation();
                        double dx = Math.abs(loc.getX() - pLoc.getX());
                        double dz = Math.abs(loc.getZ() - pLoc.getZ());
                        double dy = loc.getY() - pLoc.getY();

                        // Kollision erkennen (Spieler ist 0.6 breit und 1.8 hoch)
                        if (dx < 0.9 && dz < 0.9 && dy >= 0.0 && dy <= 2.2) {
                            // 8.0 HP = 4 Herzen Schaden zufügen
                            player.damage(8.0);
                            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);

                            // Amboss-Entity löschen, um Spam auf dem Boden zu vermeiden
                            anvil.remove();
                            it.remove();
                            break;
                        }
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 1L, 1L);
    }

    @Override
    public void onDisable() {
        if (spawnTask != null) {
            spawnTask.cancel();
            spawnTask = null;
        }
        if (collisionTask != null) {
            collisionTask.cancel();
            collisionTask = null;
        }
        activeAnvils.clear();
    }
}
