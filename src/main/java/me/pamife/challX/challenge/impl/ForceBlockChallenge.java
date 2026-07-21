package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ForceBlockChallenge extends BaseChallenge {

    private BukkitTask mainTask;
    private BukkitTask runTask;
    private BossBar bossBar;
    
    private Material targetBlock = null;
    private int timeLeft = 0;
    private final Set<UUID> safePlayers = new HashSet<>();

    private static final List<Material> VALID_BLOCKS = Arrays.asList(
            Material.GRASS_BLOCK, Material.DIRT, Material.COBBLESTONE, Material.STONE, Material.OAK_PLANKS,
            Material.SAND, Material.GRAVEL, Material.OAK_LOG, Material.GLASS, Material.WHITE_WOOL,
            Material.CRAFTING_TABLE, Material.FURNACE, Material.CHEST, Material.COAL_ORE, Material.IRON_ORE,
            Material.COPPER_ORE, Material.GOLD_ORE, Material.DEEPSLATE, Material.OBSIDIAN, Material.NETHERRACK,
            Material.SOUL_SAND, Material.SOUL_SOIL, Material.GLOWSTONE, Material.MAGMA_BLOCK, Material.BONE_BLOCK
    );

    @Override
    public String getName() {
        return "Force-Block";
    }

    @Override
    public String getDescription() {
        return "Spieler müssen alle 3 Minuten innerhalb von 60 Sekunden auf einem bestimmten Block stehen.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a§lForce-Block");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        safePlayers.clear();
        targetBlock = null;

        // Alle 3 Minuten (3600 Ticks) eine neue Aufgabe starten
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;
                startNewRound();
            }
        }.runTaskTimer(ChallX.getInstance(), 100L, 3600L); // Erste Runde nach 5 Sekunden
    }

    private void startNewRound() {
        if (runTask != null) {
            runTask.cancel();
        }
        if (bossBar != null) {
            bossBar.removeAll();
        }

        safePlayers.clear();
        targetBlock = VALID_BLOCKS.get(new Random().nextInt(VALID_BLOCKS.size()));
        timeLeft = 60;

        bossBar = Bukkit.createBossBar("§eStehe auf: §6" + targetBlock.name() + " §7- Noch: §c60s", BarColor.YELLOW, BarStyle.SOLID);
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
                    // Timer abgelaufen: Bestrafen, wenn man nicht genau jetzt auf dem Block steht
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
                        if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;

                        Block stand = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
                        if (stand.getType() == targetBlock) {
                            p.sendMessage("§a[Force-Block] §2Erfolgreich! Du standest auf dem Block.");
                            p.sendTitle("§a§lÜberlebt!", "§eBlock gefunden.", 5, 40, 5);
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                        } else {
                            p.damage(20.0); // Sofortiger Tod
                            p.sendMessage("§c[Force-Block] Zeit abgelaufen! Du standest zum Ablauf nicht auf dem Block (" + targetBlock.name() + ").");
                        }
                    }
                    bossBar.removeAll();
                    cancel();
                } else {
                    bossBar.setTitle("§eStehe auf: §6" + targetBlock.name() + " §7- Noch: §c" + timeLeft + "s");
                    bossBar.setProgress((double) timeLeft / 60.0);
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L); // Jede Sekunde (20 Ticks)
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
        targetBlock = null;
    }
}
