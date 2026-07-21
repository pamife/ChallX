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

public class ForceItemBattleChallenge extends BaseChallenge {

    private BukkitTask task;
    private BossBar bossBar;
    private int timeLeft = 300; // 5 Minuten (300 Sekunden)

    @Override
    public String getName() {
        return "Force Item Battle";
    }

    @Override
    public String getDescription() {
        return "Sammelt innerhalb von 5 Minuten so viele verschiedene Items wie möglich. Wer am meisten hat, gewinnt.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lForce Item Battle");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        timeLeft = 300;
        bossBar = Bukkit.createBossBar("§e§lForce Item Battle §7- Restzeit: §c5:00", BarColor.YELLOW, BarStyle.SOLID);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) {
                bossBar.addPlayer(p);
            }
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                timeLeft--;
                if (timeLeft <= 0) {
                    evaluateWinner();
                    bossBar.removeAll();
                    cancel();
                } else {
                    int min = timeLeft / 60;
                    int sec = timeLeft % 60;
                    bossBar.setTitle(String.format("§e§lForce Item Battle §7- Restzeit: §c%d:%02d", min, sec));
                    bossBar.setProgress((double) timeLeft / 300.0);
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L);
    }

    private void evaluateWinner() {
        Player winner = null;
        int maxUnique = -1;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
            if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) continue;

            Set<Material> uniqueItems = new HashSet<>();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    uniqueItems.add(item.getType());
                }
            }

            int count = uniqueItems.size();
            player.sendMessage("§e[Battle] Du hast §6" + count + " §eveschiedene Items gesammelt!");

            if (count > maxUnique) {
                maxUnique = count;
                winner = player;
            }
        }

        if (winner != null) {
            Bukkit.broadcastMessage("§a🏆 §l" + winner.getName() + " §agewinnt das Force Item Battle mit §6" + maxUnique + " §averschiedenen Items!");
            winner.sendTitle("§a§lSIEG!", "§eMit " + maxUnique + " Items gewonnen!", 10, 60, 20);
            winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }
}
