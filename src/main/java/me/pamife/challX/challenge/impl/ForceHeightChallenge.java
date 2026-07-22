package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
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
import java.util.Random;

public class ForceHeightChallenge extends BaseChallenge {

    private BukkitTask runTask;
    private int targetHeight;
    private int timeLeft;
    private int customDuration = 60; // Default 60s
    private BossBar bossBar;

    @Override
    public String getName() {
        return "Force-Height";
    }

    @Override
    public String getDescription() {
        return "Du musst beim Ablauf des Timers auf der vorgegebenen Höhe (Y-Koordinate) stehen. Zeit im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.LADDER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lForce-Height");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public void openSettings(Player player) {
        CustomGUI gui = new CustomGUI(Component.text("§e§lForce-Height Countdown Zeit"), 3);

        int[] times = {30, 45, 60, 90};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < times.length; i++) {
            int t = times[i];
            ItemStack item = createSettingsItem(
                    Material.CLOCK,
                    "§e§l" + t + " Sekunden Zeit",
                    "§7Zeit bis zum Höhen-Check: " + t + "s.",
                    "",
                    t == customDuration ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                customDuration = t;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                openSettings(player);
            }));
        }

        gui.setButton(22, new GUIButton(
                createSettingsItem(Material.BARRIER, "§cZurück zu Challenges"),
                e -> ChallX.getInstance().openChallengesGUI(player)
        ));

        fillBackground(gui);
        gui.open(player);
    }

    private ItemStack createSettingsItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillBackground(CustomGUI gui) {
        ItemStack filler = createSettingsItem(Material.GRAY_STAINED_GLASS_PANE, "§7 ");
        for (int i = 0; i < 27; i++) {
            if (gui.getButton(i) == null) {
                gui.setButton(i, new GUIButton(filler, e -> {}));
            }
        }
    }

    @Override
    public void onEnable() {
        startNewRound();
    }

    private void startNewRound() {
        if (runTask != null) runTask.cancel();
        if (bossBar != null) bossBar.removeAll();

        // Ziel-Höhe zwischen Y=60 und Y=120
        targetHeight = 60 + new Random().nextInt(60);
        timeLeft = customDuration;

        bossBar = Bukkit.createBossBar("§eErreiche Höhe: Y=§6" + targetHeight + " §7- Noch: §c" + timeLeft + "s", BarColor.YELLOW, BarStyle.SOLID);
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
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
                        if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;

                        int currentY = p.getLocation().getBlockY();
                        if (Math.abs(currentY - targetHeight) <= 1) {
                            p.sendMessage("§a[Force-Height] §2Erfolgreich! Du stehst auf Höhe Y=" + targetHeight + ".");
                            p.sendTitle("§a§lÜberlebt!", "§eHöhe erreicht.", 5, 40, 5);
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                        } else {
                            p.damage(20.0);
                            p.sendMessage("§c[Force-Height] Zeit abgelaufen! Du standest auf Y=" + currentY + " statt Y=" + targetHeight + ".");
                        }
                    }
                    bossBar.removeAll();
                    cancel();
                } else {
                    bossBar.setTitle("§eErreiche Höhe: Y=§6" + targetHeight + " §7- Noch: §c" + timeLeft + "s");
                    bossBar.setProgress((double) timeLeft / (double) customDuration);
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L);
    }

    @Override
    public void onDisable() {
        if (runTask != null) {
            runTask.cancel();
            runTask = null;
        }
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    @Override
    public Object getSettingsState() {
        return customDuration;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            customDuration = num.intValue();
        }
    }
}
