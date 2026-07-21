package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Random;

public class TrafficLightChallenge extends BaseChallenge {

    private enum LightState {
        GREEN("§a§lGRÜN - BEWEGUNG ERLAUBT", BarColor.GREEN, 12),
        YELLOW("§e§lGELB - ACHTUNG!", BarColor.YELLOW, 3),
        RED("§c§lROT - STEHEN BLEIBEN!", BarColor.RED, 7);

        final String title;
        final BarColor color;
        final int duration; // in Sekunden

        LightState(String title, BarColor color, int duration) {
            this.title = title;
            this.color = color;
            this.duration = duration;
        }
    }

    private BossBar bossBar;
    private BukkitTask task;
    private LightState currentState = LightState.GREEN;
    private int ticksLeft = 0;

    @Override
    public String getName() {
        return "Ampel-Challenge";
    }

    @Override
    public String getDescription() {
        return "Zeigt am oberen Rand eine Ampel. Wer sich bei Rot bewegt, stirbt.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.REDSTONE_LAMP);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cAmpel-Challenge");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        bossBar = Bukkit.createBossBar(LightState.GREEN.title, BarColor.GREEN, BarStyle.SOLID);
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(p);
        }

        currentState = LightState.GREEN;
        ticksLeft = currentState.duration * 20;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) {
                    bossBar.setTitle(currentState.title + " §7(Pausiert)");
                    return;
                }

                if (ticksLeft > 0) {
                    ticksLeft--;
                    double progress = (double) ticksLeft / (currentState.duration * 20.0);
                    bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
                } else {
                    // Zustandswechsel
                    switchState();
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 1L, 1L);
    }

    private void switchState() {
        switch (currentState) {
            case GREEN -> {
                currentState = LightState.YELLOW;
                playSoundToAll(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            }
            case YELLOW -> {
                currentState = LightState.RED;
                playSoundToAll(Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
            }
            case RED -> {
                currentState = LightState.GREEN;
                playSoundToAll(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
        }
        ticksLeft = currentState.duration * 20;
        bossBar.setColor(currentState.color);
        bossBar.setTitle(currentState.title);
    }

    private void playSoundToAll(Sound sound, float vol, float pitch) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), sound, vol, pitch);
        }
    }

    @Override
    public void onDisable() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (isEnabled() && bossBar != null) {
            bossBar.addPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;
        if (currentState != LightState.RED) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        // Horizontale Bewegung prüfen (X und Z)
        if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
            player.setHealth(0.0);
            Bukkit.broadcastMessage("§c" + player.getName() + " ist bei ROT gelaufen und gestorben!");
        }
    }
}
