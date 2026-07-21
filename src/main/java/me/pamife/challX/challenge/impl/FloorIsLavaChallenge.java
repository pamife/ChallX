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

public class FloorIsLavaChallenge extends BaseChallenge {

    private BukkitTask task;

    @Override
    public String getName() {
        return "Der Boden ist Lava";
    }

    @Override
    public String getDescription() {
        return "Der Boden unter den Spielern wird erst zu Magma und danach zu Lava.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.MAGMA_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Der Boden ist Lava");
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
                    if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;

                    Block blockUnder = player.getLocation().clone().subtract(0, 0.01, 0).getBlock();
                    Material type = blockUnder.getType();

                    // Wenn der Block solide ist und kein Luft/Magma/Lava/Bedrock/Portal
                    if (type.isSolid() && type != Material.MAGMA_BLOCK && type != Material.LAVA && type != Material.BEDROCK && type != Material.BARRIER) {
                        final Block targetBlock = blockUnder;
                        // Zu Magma nach 15 Ticks (0.75 Sekunden)
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!isEnabled() || !ChallX.getInstance().getTimerManager().isRunning()) return;
                                if (targetBlock.getType() != Material.BEDROCK && targetBlock.getType() != Material.BARRIER && targetBlock.getType() != Material.LAVA) {
                                    targetBlock.setType(Material.MAGMA_BLOCK);
                                    
                                    // Zu Lava nach weiteren 15 Ticks (0.75 Sekunden)
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            if (!isEnabled() || !ChallX.getInstance().getTimerManager().isRunning()) return;
                                            if (targetBlock.getType() == Material.MAGMA_BLOCK) {
                                                targetBlock.setType(Material.LAVA);
                                            }
                                        }
                                    }.runTaskLater(ChallX.getInstance(), 15L);
                                }
                            }
                        }.runTaskLater(ChallX.getInstance(), 15L);
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
