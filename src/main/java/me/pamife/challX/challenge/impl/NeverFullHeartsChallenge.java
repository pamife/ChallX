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
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;

public class NeverFullHeartsChallenge extends BaseChallenge {

    private BukkitTask task;
    private int graceTimerConfig = 10; // Default 10 Sekunden
    private int currentGraceTimeLeft = 10;
    private boolean gracePeriodActive = true;
    private BossBar graceBossBar;

    @Override
    public String getName() {
        return "Niemals volle Herzen";
    }

    @Override
    public String getDescription() {
        return "Volle Herzen führen sofort zum Tod. Zu Beginn existiert eine anpassbare Schutzzeit.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.WITHER_ROSE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lNiemals volle Herzen");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lVolle Herzen Schutzzeit"), 3);

        int[] times = {5, 10, 20, 30};
        int[] slots = {10, 11, 12, 13};
        
        for (int i = 0; i < times.length; i++) {
            int t = times[i];
            ItemStack icon = createSettingsItem(
                    Material.CLOCK, 
                    "§e§l" + t + " Sekunden", 
                    "§7Setzt die Schutzzeit auf " + t + "s.", 
                    "", 
                    t == graceTimerConfig ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(icon, e -> {
                graceTimerConfig = t;
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
        currentGraceTimeLeft = graceTimerConfig;
        gracePeriodActive = true;

        graceBossBar = Bukkit.createBossBar("§e[Volle Herzen] Schutzzeit: §c" + currentGraceTimeLeft + "s §7(Nimm Schaden!)", BarColor.YELLOW, BarStyle.SOLID);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) {
                graceBossBar.addPlayer(p);
            }
        }

        task = new BukkitRunnable() {
            int tickCounter = 0;

            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                tickCounter++;
                if (tickCounter >= 2) {
                    tickCounter = 0;
                    if (gracePeriodActive && currentGraceTimeLeft > 0) {
                        currentGraceTimeLeft--;
                        
                        if (graceBossBar != null) {
                            graceBossBar.setTitle("§e[Volle Herzen] Schutzzeit: §c" + currentGraceTimeLeft + "s §7(Nimm Schaden!)");
                            graceBossBar.setProgress((double) currentGraceTimeLeft / (double) graceTimerConfig);
                        }

                        if (currentGraceTimeLeft <= 0) {
                            gracePeriodActive = false;
                            if (graceBossBar != null) {
                                graceBossBar.removeAll();
                                graceBossBar = null;
                            }
                            Bukkit.broadcastMessage("§c[Volle Herzen] Die Schutzzeit ist abgelaufen!");
                        }
                    }
                }

                if (!gracePeriodActive) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
                        if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;

                        var maxHealthAttr = p.getAttribute(Attribute.MAX_HEALTH);
                        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;

                        if (p.getHealth() >= maxHealth - 0.1) {
                            p.setHealth(0.0);
                            Bukkit.broadcastMessage("§c[Volle Herzen] " + p.getName() + " ist an vollen Herzen gestorben!");
                        }
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 10L, 10L);
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (graceBossBar != null) {
            graceBossBar.removeAll();
            graceBossBar = null;
        }
        gracePeriodActive = false;
    }

    @Override
    public Object getSettingsState() {
        return graceTimerConfig;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            graceTimerConfig = num.intValue();
        }
    }
}
