package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class AnvilRainChallenge extends BaseChallenge {

    private BukkitTask task;

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
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                    if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;

                    // Amboss 15 Blöcke über dem Spieler spawnen
                    Location spawnLoc = player.getLocation().clone().add(0, 15, 0);
                    player.getWorld().spawnFallingBlock(spawnLoc, Bukkit.createBlockData(Material.ANVIL));
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 60L, 60L);
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @EventHandler
    public void onAnvilDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (event.getDamager() instanceof FallingBlock fallingBlock) {
            if (fallingBlock.getBlockData().getMaterial() == Material.ANVIL) {
                event.setCancelled(true);
            }
        }
    }
}
