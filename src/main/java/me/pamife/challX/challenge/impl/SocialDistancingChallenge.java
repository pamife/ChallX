package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class SocialDistancingChallenge extends BaseChallenge {

    private BukkitTask task;

    @Override
    public String getName() {
        return "Social Distancing";
    }

    @Override
    public String getDescription() {
        return "Spieler müssen 3 Blöcke Abstand zu allen Kreaturen und Mitspielern halten.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eSocial Distancing");
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
                    if (player.isDead()) continue;

                    boolean tooClose = false;
                    for (Entity nearby : player.getNearbyEntities(3.0, 3.0, 3.0)) {
                        if (nearby instanceof LivingEntity living && !(nearby instanceof ArmorStand) && !living.isDead()) {
                            // Wenn es ein anderer Spieler ist, prüfen ob dieser Spectator/Excluded ist
                            if (living instanceof Player otherPlayer) {
                                if (ChallX.getInstance().getSettingsManager().isExcluded(otherPlayer.getUniqueId()) ||
                                    otherPlayer.getGameMode() == org.bukkit.GameMode.SPECTATOR ||
                                    otherPlayer.getGameMode() == org.bukkit.GameMode.CREATIVE) {
                                    continue;
                                }
                            }
                            
                            tooClose = true;
                            break;
                        }
                    }

                    if (tooClose) {
                        player.damage(2.0); // 1 Herz Schaden
                        player.sendMessage("§cSocial Distancing! Halte mindestens 3 Blöcke Abstand zu Kreaturen!");
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L);
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
