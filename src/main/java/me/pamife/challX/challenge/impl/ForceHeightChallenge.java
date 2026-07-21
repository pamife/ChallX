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

import java.util.*;

public class ForceHeightChallenge extends BaseChallenge {

    private BukkitTask mainTask;
    private BukkitTask runTask;
    private BossBar bossBar;

    private int targetHeight = 0;
    private int timeLeft = 0;
    private final Set<UUID> safePlayers = new HashSet<>();

    @Override
    public String getName() {
        return "Force-Height";
    }

    @Override
    public String getDescription() {
        return "Spieler müssen alle 3 Minuten eine bestimmte Y-Höhe innerhalb von 60 Sekunden erreichen.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.SCAFFOLDING);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lForce-Height");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        safePlayers.clear();
        targetHeight = 0;

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

        safePlayers.clear();

        // Target ermitteln
        Player targetPlayer = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
            if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;
            targetPlayer = p;
            break;
        }

        if (targetPlayer == null) return;

        // Y-Ziel relativ zum Spieler wählen (-15 bis +15)
        int currentY = targetPlayer.getLocation().getBlockY();
        int offset = -15 + new Random().nextInt(31);
        if (offset == 0) offset = 5; // Verhindert 0 Offset
        targetHeight = currentY + offset;
        timeLeft = 60;

        bossBar = Bukkit.createBossBar("§eErreiche Höhe: §6Y = " + targetHeight + " §7- Noch: §c60s", BarColor.BLUE, BarStyle.SOLID);
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
                    // Timer abgelaufen: Bestrafen, wenn man nicht genau jetzt auf der Ziel-Höhe ist
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
                        if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;

                        if (p.getLocation().getBlockY() == targetHeight) {
                            p.sendMessage("§a[Force-Height] §2Erfolgreich! Du hast die Höhe erreicht.");
                            p.sendTitle("§a§lÜberlebt!", "§eHöhe erreicht.", 5, 40, 5);
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                        } else {
                            p.damage(20.0);
                            p.sendMessage("§c[Force-Height] Zeit abgelaufen! Du warst zum Ablauf nicht auf der Höhe Y = " + targetHeight + ".");
                        }
                    }
                    bossBar.removeAll();
                    cancel();
                } else {
                    bossBar.setTitle("§eErreiche Höhe: §6Y = " + targetHeight + " §7- Noch: §c" + timeLeft + "s");
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
        safePlayers.clear();
    }
}
