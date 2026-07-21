package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class RandomScaleChallenge extends BaseChallenge {

    private BukkitTask task;

    @Override
    public String getName() {
        return "Größenänderung";
    }

    @Override
    public String getDescription() {
        return "Spieler werden alle 2 Minuten zufällig winzig klein (0.3x) oder riesengroß (3.0x).";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.CHORUS_FRUIT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§dGrößenänderung");
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

                    // Skalierung zwischen 0.3 (30%) und 3.0 (300%)
                    double scale = 0.3 + (random.nextDouble() * 2.7);

                    AttributeInstance scaleAttr = player.getAttribute(Attribute.SCALE);
                    if (scaleAttr != null) {
                        scaleAttr.setBaseValue(scale);
                        player.sendMessage("§d[Größe] §7Deine Größe wurde auf §e" + String.format("%.2f", scale) + "x §7skaliert!");
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 2400L, 2400L); // Alle 2 Minuten (2400 Ticks)
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        // Alle Spieler zurückskalieren auf 1.0
        for (Player player : Bukkit.getOnlinePlayers()) {
            AttributeInstance scaleAttr = player.getAttribute(Attribute.SCALE);
            if (scaleAttr != null) {
                scaleAttr.setBaseValue(1.0);
            }
        }
    }
}
