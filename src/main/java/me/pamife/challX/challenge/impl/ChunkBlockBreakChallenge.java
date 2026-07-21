package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChunkBlockBreakChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Chunk Block Abbau";
    }

    @Override
    public String getDescription() {
        return "Baut ein Spieler einen Block ab, werden alle Blöcke desselben Typs im ganzen Chunk zerstört.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.TNT_MINECART);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lChunk Block Abbau");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        Block targetBlock = event.getBlock();
        Material typeToClear = targetBlock.getType();

        if (typeToClear == Material.AIR || typeToClear == Material.BEDROCK) return;

        Chunk chunk = targetBlock.getChunk();
        World world = chunk.getWorld();

        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        // Alle 16x16 Blöcke im Chunk durchsuchen
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    Block b = chunk.getBlock(x, y, z);
                    if (b.getType() == typeToClear) {
                        b.setType(Material.AIR);
                    }
                }
            }
        }
    }
}
