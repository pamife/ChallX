package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;

public class OnlyDirtChallenge extends BaseChallenge {

    private BukkitTask task;
    private final List<Material> allowedBlocks = Arrays.asList(
            Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, 
            Material.ROOTED_DIRT, Material.PODZOL, Material.MYCELIUM
    );

    @Override
    public String getName() {
        return "Nur Dirt";
    }

    @Override
    public String getDescription() {
        return "Die Spieler sterben, sobald sie nicht auf Dirt/Gras stehen.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aNur Dirt");
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
                    if (player.isDead()) continue;
                    if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;

                    // Nur prüfen, wenn der Spieler auf dem Boden steht
                    if (player.isOnGround()) {
                        Block blockUnder = player.getLocation().clone().subtract(0, 0.01, 0).getBlock();
                        Material type = blockUnder.getType();
                        
                        // Wenn der Block unter dem Spieler kein Dirt ist und nicht Luft/Wasser/Lava
                        if (!allowedBlocks.contains(type) && !type.isAir()) {
                            player.setHealth(0.0);
                            Bukkit.broadcastMessage("§c" + player.getName() + " ist nicht auf Dirt gestanden!");
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
    }
}
