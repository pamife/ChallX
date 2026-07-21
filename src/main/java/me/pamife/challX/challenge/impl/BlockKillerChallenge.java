package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockKillerChallenge extends BaseChallenge {

    private BukkitTask task;
    private final Map<UUID, Material> lastBlockType = new HashMap<>();

    @Override
    public String getName() {
        return "Block Killer";
    }

    @Override
    public String getDescription() {
        return "Alle Blöcke des Typs, auf dem du stehst, werden im ganzen Chunk gelöscht.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.GOLDEN_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lBlock Killer");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        lastBlockType.clear();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                    if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) continue;

                    Block standingOn = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
                    Material targetType = standingOn.getType();

                    if (targetType == Material.AIR || targetType == Material.VOID_AIR || targetType == Material.CAVE_AIR) continue;

                    Material last = lastBlockType.get(player.getUniqueId());
                    if (last == null || last != targetType) {
                        lastBlockType.put(player.getUniqueId(), targetType);

                        // Gesamten Chunk durchsuchen und diesen Block-Typ löschen
                        Chunk chunk = standingOn.getChunk();
                        int minHeight = chunk.getWorld().getMinHeight();
                        int maxHeight = chunk.getWorld().getMaxHeight();

                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                for (int y = minHeight; y < maxHeight; y++) {
                                    Block b = chunk.getBlock(x, y, z);
                                    if (b.getType() == targetType) {
                                        b.setType(Material.AIR);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 5L, 5L);
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        lastBlockType.clear();
    }
}
