package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;

public class SpeedChallenge extends BaseChallenge {

    private BukkitTask task;
    private int speedLevel = 2; // Default Speed II

    @Override
    public String getName() {
        return "Speed";
    }

    @Override
    public String getDescription() {
        return "Alle Entities in der Welt bewegen sich mit erhöhter Geschwindigkeit. Stufe einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.SUGAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§f§lSpeed");
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
        CustomGUI gui = new CustomGUI(Component.text("§f§lSpeed Stufe wählen"), 3);

        int[] levels = {1, 2, 3, 5};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < levels.length; i++) {
            int lvl = levels[i];
            ItemStack item = createSettingsItem(
                    Material.SUGAR,
                    "§e§lSpeed Stufe " + lvl,
                    "§7Setzt die Geschwindigkeit auf Stufe " + lvl + ".",
                    "",
                    lvl == speedLevel ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                speedLevel = lvl;
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

                PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, 40, speedLevel - 1, false, false, false);
                for (World world : Bukkit.getWorlds()) {
                    for (LivingEntity entity : world.getLivingEntities()) {
                        entity.addPotionEffect(effect);
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L); // Alle 1 Sekunde auffrischen
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public Object getSettingsState() {
        return speedLevel;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            speedLevel = num.intValue();
        }
    }
}
