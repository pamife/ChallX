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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ForceItemChallenge extends BaseChallenge {

    private BukkitTask runTask;
    private Material targetItem;
    private int timeLeft;
    private int customDuration = 60; // Default 60s
    private BossBar bossBar;

    private static final List<Material> VALID_ITEMS = new ArrayList<>();

    static {
        for (Material m : Material.values()) {
            if (!m.isLegacy() && m != Material.AIR && m != Material.BEDROCK && m != Material.BARRIER && m.isItem()) {
                VALID_ITEMS.add(m);
            }
        }
    }

    @Override
    public String getName() {
        return "Force-Item";
    }

    @Override
    public String getDescription() {
        return "Du musst beim Ablauf des Timers das gesuchte Item im Inventar haben. Zeit im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lForce-Item");
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
        CustomGUI gui = new CustomGUI(Component.text("§b§lForce-Item Countdown Zeit"), 3);

        int[] times = {30, 45, 60, 90};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < times.length; i++) {
            int t = times[i];
            ItemStack item = createSettingsItem(
                    Material.CLOCK,
                    "§e§l" + t + " Sekunden Zeit",
                    "§7Zeit bis zum Item-Check: " + t + "s.",
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

        targetItem = VALID_ITEMS.get(new Random().nextInt(VALID_ITEMS.size()));
        timeLeft = customDuration;

        bossBar = Bukkit.createBossBar("§eFinde Item: §6" + targetItem.name() + " §7- Noch: §c" + timeLeft + "s", BarColor.BLUE, BarStyle.SOLID);
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

                        if (p.getInventory().contains(targetItem)) {
                            p.sendMessage("§a[Force-Item] §2Erfolgreich! Du hast das Item im Inventar.");
                            p.sendTitle("§a§lÜberlebt!", "§eItem gefunden.", 5, 40, 5);
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                        } else {
                            p.damage(20.0);
                            p.sendMessage("§c[Force-Item] Zeit abgelaufen! Du hattest " + targetItem.name() + " nicht im Inventar.");
                        }
                    }
                    bossBar.removeAll();
                    cancel();
                } else {
                    bossBar.setTitle("§eFinde Item: §6" + targetItem.name() + " §7- Noch: §c" + timeLeft + "s");
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
