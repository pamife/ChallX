package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
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

public class ForceBiomeChallenge extends BaseChallenge {

    private BukkitTask mainTask;
    private BukkitTask runTask;
    private BossBar bossBar;

    private Biome targetBiome = null;
    private int timeLeft = 0;

    private static final List<Biome> VALID_BIOMES = Arrays.asList(
            Biome.PLAINS, Biome.DESERT, Biome.FOREST, Biome.TAIGA, Biome.SWAMP,
            Biome.JUNGLE, Biome.SAVANNA, Biome.BADLANDS, Biome.RIVER, Biome.OCEAN,
            Biome.SNOWY_PLAINS, Biome.DARK_FOREST, Biome.MEADOW, Biome.BIRCH_FOREST
    );

    @Override
    public String getName() {
        return "Force-Biome";
    }

    @Override
    public String getDescription() {
        return "Spieler müssen alle 3 Minuten innerhalb von 60 Sekunden in einem bestimmten Biom stehen.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.OAK_SAPLING);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a§lForce-Biome");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        targetBiome = null;

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

        targetBiome = VALID_BIOMES.get(new Random().nextInt(VALID_BIOMES.size()));
        timeLeft = 60;

        bossBar = Bukkit.createBossBar("§eGehe in Biom: §6" + targetBiome.toString() + " §7- Noch: §c60s", BarColor.GREEN, BarStyle.SOLID);
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

                        Biome currentBiome = p.getLocation().getBlock().getBiome();
                        if (currentBiome == targetBiome) {
                            p.sendMessage("§a[Force-Biome] §2Erfolgreich! Du stehst im Biom.");
                            p.sendTitle("§a§lÜberlebt!", "§eBiom gefunden.", 5, 40, 5);
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                        } else {
                            p.damage(20.0);
                            p.sendMessage("§c[Force-Biome] Zeit abgelaufen! Du warst zum Ablauf nicht im Biom (" + targetBiome.toString() + ").");
                        }
                    }
                    bossBar.removeAll();
                    cancel();
                } else {
                    bossBar.setTitle("§eGehe in Biom: §6" + targetBiome.toString() + " §7- Noch: §c" + timeLeft + "s");
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
