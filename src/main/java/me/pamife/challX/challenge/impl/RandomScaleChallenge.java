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
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Random;

public class RandomScaleChallenge extends BaseChallenge {

    private BukkitTask task;
    private int scaleMode = 0; // 0 = Normal (0.5x - 2.0x), 1 = Extrem (0.2x - 3.5x)

    @Override
    public String getName() {
        return "Zufalls-Größe";
    }

    @Override
    public String getDescription() {
        return "Die Körpergröße der Spieler ändert sich regelmäßig zufällig. Bereich im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.PUFFERFISH);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§d§lZufalls-Größe");
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
        CustomGUI gui = new CustomGUI(Component.text("§d§lGrößen-Spannweite"), 3);

        int[] modes = {0, 1};
        String[] labels = {"Normal (0.5x - 2.0x)", "Extrem (0.2x - 3.5x)"};
        int[] slots = {11, 15};

        for (int i = 0; i < modes.length; i++) {
            int m = modes[i];
            ItemStack item = createSettingsItem(
                    Material.PUFFERFISH,
                    "§e§l" + labels[i],
                    "§7Größenbereich: " + labels[i],
                    "",
                    m == scaleMode ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                scaleMode = m;
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
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                Random random = new Random();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                    if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) continue;

                    double min = scaleMode == 0 ? 0.5 : 0.2;
                    double max = scaleMode == 0 ? 2.0 : 3.5;
                    double randomScale = min + (max - min) * random.nextDouble();

                    AttributeInstance scaleAttr = player.getAttribute(Attribute.SCALE);
                    if (scaleAttr != null) {
                        scaleAttr.setBaseValue(randomScale);
                        player.sendMessage("§d[Zufalls-Größe] Deine Skalierung wurde auf §6" + String.format("%.2f", randomScale) + "x §dgeändert!");
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 200L, 200L);
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            AttributeInstance scaleAttr = p.getAttribute(Attribute.SCALE);
            if (scaleAttr != null) {
                scaleAttr.setBaseValue(1.0);
            }
        }
    }

    @Override
    public Object getSettingsState() {
        return scaleMode;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            scaleMode = num.intValue();
        }
    }
}
