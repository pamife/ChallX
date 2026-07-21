package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ChaosPlacementChallenge extends BaseChallenge {

    private BukkitTask task;
    private static final List<Material> CHAOS_BLOCKS = Arrays.asList(
            Material.COBWEB,
            Material.TNT,
            Material.SLIME_BLOCK,
            Material.ICE,
            Material.MAGMA_BLOCK,
            Material.OBSIDIAN,
            Material.HONEY_BLOCK
    );

    @Override
    public String getName() {
        return "Chaos-Block-Spawn";
    }

    @Override
    public String getDescription() {
        return "Spawnt alle 15 Sekunden einen zufälligen Block unter den Füßen der Spieler, der nach 4s wieder verschwindet.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.TNT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cChaos-Block-Spawn");
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

                Random random = new Random();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                    if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;

                    Block block = player.getLocation().getBlock();
                    // Wenn der Block Luft oder Wasser ist, platzieren wir dort
                    BlockState originalState = block.getState();
                    Material randomMaterial = CHAOS_BLOCKS.get(random.nextInt(CHAOS_BLOCKS.size()));
                    
                    block.setType(randomMaterial);

                    // Nach 4 Sekunden zurücksetzen
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (isEnabled()) {
                                originalState.update(true, false);
                            }
                        }
                    }.runTaskLater(ChallX.getInstance(), 80L); // 80 Ticks = 4 Sekunden
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 300L, 300L); // Alle 15 Sekunden (300 Ticks)
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
