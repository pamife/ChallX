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

import java.util.*;

public class ForceItemBattleChallenge extends BaseChallenge {

    private BukkitTask runTask;
    private int timeLeft;
    private int battleMinutes = 5; // Default 5 Min (300s)
    private BossBar bossBar;

    @Override
    public String getName() {
        return "Force Item Battle";
    }

    @Override
    public String getDescription() {
        return "Sammelt gegeneinander die meisten unterschiedlichen Items in der vorgegebenen Zeit. Dauer im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lForce Item Battle");
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
        CustomGUI gui = new CustomGUI(Component.text("§b§lForce Item Battle Dauer"), 3);

        int[] mins = {3, 5, 10, 15};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < mins.length; i++) {
            int m = mins[i];
            ItemStack item = createSettingsItem(
                    Material.CLOCK,
                    "§e§l" + m + " Minuten Battle",
                    "§7Rundenzeit: " + m + " Minuten.",
                    "",
                    m == battleMinutes ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                battleMinutes = m;
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
        if (runTask != null) runTask.cancel();
        if (bossBar != null) bossBar.removeAll();

        timeLeft = battleMinutes * 60;
        bossBar = Bukkit.createBossBar("§bForce Item Battle - Noch: §c" + timeLeft + "s", BarColor.PURPLE, BarStyle.SOLID);
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
                    // Auswertung
                    Player winner = null;
                    int maxUnique = -1;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
                        if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;

                        Set<Material> uniqueItems = new HashSet<>();
                        for (ItemStack stack : p.getInventory().getContents()) {
                            if (stack != null && stack.getType() != Material.AIR) {
                                uniqueItems.add(stack.getType());
                            }
                        }

                        if (uniqueItems.size() > maxUnique) {
                            maxUnique = uniqueItems.size();
                            winner = p;
                        }
                    }

                    if (winner != null) {
                        Bukkit.broadcastMessage("§b[Force Item Battle] §a" + winner.getName() + " §egewinnt mit §6" + maxUnique + " §everschiedenen Items!");
                        winner.sendTitle("§a§lSIEG!", "§eMost Unique Items: " + maxUnique, 10, 80, 20);
                        winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    }
                    bossBar.removeAll();
                    cancel();
                } else {
                    bossBar.setTitle("§bForce Item Battle - Noch: §c" + timeLeft + "s");
                    bossBar.setProgress((double) timeLeft / (double) (battleMinutes * 60));
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
        return battleMinutes;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            battleMinutes = num.intValue();
        }
    }
}
