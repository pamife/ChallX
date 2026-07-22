package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Random;

public class TrafficLightChallenge extends BaseChallenge {

    private BukkitRunnable currentTask;
    private boolean isGreen = true;
    private boolean showBossBar = true; // Default true
    private int minPhaseSeconds = 3;
    private int maxPhaseSeconds = 10;
    private BossBar bossBar;

    @Override
    public String getName() {
        return "Ampel";
    }

    @Override
    public String getDescription() {
        return "Zufällige Ampelphasen (Rot/Grün). Bossbar-Countdown und Phasenbereich im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a§lAmpel");
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
        CustomGUI gui = new CustomGUI(Component.text("§a§lAmpel Einstellungen"), 3);

        // 1. BossBar Toggle (Slot 11)
        ItemStack bossBarItem = createSettingsItem(
                Material.NAME_TAG,
                "§e§lBossbar Anzeige",
                "§7Zeigt die verbleibende Phasenzeit oben an.",
                "",
                showBossBar ? "§a§lStatus: Aktiviert" : "§c§lStatus: Deaktiviert",
                "§7[Klicke zum Umschalten]"
        );
        gui.setButton(11, new GUIButton(bossBarItem, e -> {
            showBossBar = !showBossBar;
            if (!showBossBar && bossBar != null) {
                bossBar.removeAll();
            } else if (showBossBar && isEnabled()) {
                updateBossBarPlayerVisibility();
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            openSettings(player);
        }));

        // 2. Zufallsbereich (Slot 15)
        ItemStack rangeItem = createSettingsItem(
                Material.CLOCK,
                "§e§lZufalls-Dauer: " + minPhaseSeconds + "s - " + maxPhaseSeconds + "s",
                "§7Die Ampel schaltet in zufälligen Abständen um.",
                "",
                "§7[Klicke zum Ändern]"
        );
        gui.setButton(15, new GUIButton(rangeItem, e -> {
            if (minPhaseSeconds == 3) {
                minPhaseSeconds = 1;
                maxPhaseSeconds = 5;
            } else if (minPhaseSeconds == 1) {
                minPhaseSeconds = 5;
                maxPhaseSeconds = 15;
            } else {
                minPhaseSeconds = 3;
                maxPhaseSeconds = 10;
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            openSettings(player);
        }));

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
        isGreen = true;
        bossBar = Bukkit.createBossBar("§a§lGRÜN - Laufen erlaubt!", BarColor.GREEN, BarStyle.SOLID);
        updateBossBarPlayerVisibility();
        scheduleNextPhase();
    }

    private void updateBossBarPlayerVisibility() {
        if (bossBar == null) return;
        bossBar.removeAll();
        if (showBossBar) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) {
                    bossBar.addPlayer(p);
                }
            }
        }
    }

    private void scheduleNextPhase() {
        if (currentTask != null) currentTask.cancel();

        Random random = new Random();
        int randomSeconds = minPhaseSeconds + random.nextInt(Math.max(1, maxPhaseSeconds - minPhaseSeconds + 1));
        final int totalDuration = randomSeconds;

        currentTask = new BukkitRunnable() {
            int remainingSeconds = totalDuration;

            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                if (remainingSeconds <= 0) {
                    isGreen = !isGreen;
                    if (isGreen) {
                        if (bossBar != null) {
                            bossBar.setTitle("§a§lGRÜN - Laufen erlaubt!");
                            bossBar.setColor(BarColor.GREEN);
                        }
                        for (Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    } else {
                        if (bossBar != null) {
                            bossBar.setTitle("§c§lROT - STILLSTEHEN!");
                            bossBar.setColor(BarColor.RED);
                        }
                        for (Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                    }
                    scheduleNextPhase();
                } else {
                    if (showBossBar && bossBar != null) {
                        String title = isGreen ? "§a§lGRÜN - Laufen erlaubt! (§c" + remainingSeconds + "s§a)" : "§c§lROT - STILLSTEHEN! (§c" + remainingSeconds + "s§c)";
                        bossBar.setTitle(title);
                        bossBar.setProgress((double) remainingSeconds / (double) totalDuration);
                    }
                    remainingSeconds--;
                }
            }
        };
        currentTask.runTaskTimer(ChallX.getInstance(), 0L, 20L);
    }

    @Override
    public void onDisable() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;
        if (isGreen) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            player.damage(20.0); // Sofortiger Tod bei Rot
            player.sendMessage("§c[Ampel] Bei ROT bewegt! Du bist gestorben.");
        }
    }

    @Override
    public Object getSettingsState() {
        return new Object[]{showBossBar, minPhaseSeconds, maxPhaseSeconds};
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Object[] arr && arr.length >= 3) {
            if (arr[0] instanceof Boolean b) showBossBar = b;
            if (arr[1] instanceof Number n1) minPhaseSeconds = n1.intValue();
            if (arr[2] instanceof Number n2) maxPhaseSeconds = n2.intValue();
        }
    }
}
