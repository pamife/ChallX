package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class NoLookingDownChallenge extends BaseChallenge {

    private BukkitTask task;

    @Override
    public String getName() {
        return "Nicht nach unten gucken";
    }

    @Override
    public String getDescription() {
        return "Spieler dürfen den Blick nicht nach unten richten (Pitch > 45°), sonst erhalten sie Schaden.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eNicht nach unten gucken");
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

                    // Pitch > 45.0 bedeutet Blick nach unten
                    if (player.getLocation().getPitch() > 45.0f) {
                        player.damage(2.0); // 1 Herz Schaden
                        player.sendMessage("§cSieh nicht nach unten!");
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
