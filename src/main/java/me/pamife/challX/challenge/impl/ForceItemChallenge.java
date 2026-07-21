package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ForceItemChallenge extends BaseChallenge {

    private BukkitTask mainTask;
    private BukkitTask runTask;
    private BossBar bossBar;

    private Material targetItem = null;
    private int timeLeft = 0;

    private static final List<Material> VALID_ITEMS = Arrays.asList(
            Material.DIRT, Material.COBBLESTONE, Material.OAK_LOG, Material.STICK, Material.APPLE,
            Material.RAW_IRON, Material.RAW_COPPER, Material.FEATHER, Material.BONE, Material.STRING,
            Material.WHEAT_SEEDS, Material.CRAFTING_TABLE, Material.TORCH, Material.SAND, Material.GRAVEL
    );

    @Override
    public String getName() {
        return "Force-Item";
    }

    @Override
    public String getDescription() {
        return "Spieler müssen alle 3 Minuten innerhalb von 60 Sekunden ein bestimmtes Item im Inventar haben.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lForce-Item");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        targetItem = null;

        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;
                startNewRound();
            }
        }.runTaskTimer(ChallX.getInstance(), 100L, 3600L);
    }

    private void startNewRound() {
        if (runTask != null) {
            runTask.cancel();
        }
        if (bossBar != null) {
            bossBar.removeAll();
        }

        targetItem = VALID_ITEMS.get(new Random().nextInt(VALID_ITEMS.size()));
        timeLeft = 60;

        bossBar = Bukkit.createBossBar("§eFinde Item: §6" + targetItem.name() + " §7- Noch: §c60s", BarColor.PURPLE, BarStyle.SOLID);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) {
                bossBar.addPlayer(p);
            }
        }

        runTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                timeLeft--;
                if (timeLeft <= 0) {
                    // Exakter Check beim Timerablauf
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
                        if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;

                        if (p.getInventory().contains(targetItem)) {
                            p.sendMessage("§a[Force-Item] §2Erfolgreich! Du hast das Item im Inventar.");
                            p.sendTitle("§a§lÜberlebt!", "§eItem gefunden.", 5, 40, 5);
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                        } else {
                            p.damage(20.0);
                            p.sendMessage("§c[Force-Item] Zeit abgelaufen! Du hattest das Item (" + targetItem.name() + ") nicht im Inventar.");
                        }
                    }
                    bossBar.removeAll();
                    cancel();
                } else {
                    bossBar.setTitle("§eFinde Item: §6" + targetItem.name() + " §7- Noch: §c" + timeLeft + "s");
                    bossBar.setProgress((double) timeLeft / 60.0);
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L);
    }

    @Override
    public void onDisable() {
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        if (runTask != null) {
            runTask.cancel();
            runTask = null;
        }
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }
}
