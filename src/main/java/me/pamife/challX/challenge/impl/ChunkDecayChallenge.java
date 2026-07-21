package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ChunkDecayChallenge extends BaseChallenge {

    private BukkitTask task;

    @Override
    public String getName() {
        return "Chunk-Abbau";
    }

    @Override
    public String getDescription() {
        return "Jede Minute wird die oberste Blockschicht in deinem Chunk abgetragen.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.TNT_MINECART);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lChunk-Abbau");
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
                    if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) continue;

                    Chunk chunk = player.getLocation().getChunk();
                    int chunkX = chunk.getX() * 16;
                    int chunkZ = chunk.getZ() * 16;

                    // Oberste Blockschicht für alle 256 Spalten löschen
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            Block highest = chunk.getWorld().getHighestBlockAt(chunkX + x, chunkZ + z);
                            
                            // Falls der Block unter Luft liegt und nicht bereits Luft ist
                            if (highest.getType() != Material.AIR) {
                                highest.setType(Material.AIR);
                            } else {
                                // Versuche einen Block tiefer zu gehen, falls highest fälschlicherweise Luft meldet
                                Block below = highest.getRelative(0, -1, 0);
                                if (below.getType() != Material.AIR) {
                                    below.setType(Material.AIR);
                                }
                            }
                        }
                    }

                    player.sendMessage("§c[Chunk-Abbau] §eDie oberste Blockschicht deines Chunks wurde gelöscht!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1.0f);
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 1200L, 1200L); // Alle 60 Sekunden (1200 Ticks)
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
