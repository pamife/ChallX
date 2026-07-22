package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class LevelBorderChallenge extends BaseChallenge {

    private int highestLevelReached = 0;
    private double levelMultiplier = 2.0; // Default 2.0 Blöcke pro Level

    @Override
    public String getName() {
        return "Level = Border";
    }

    @Override
    public String getDescription() {
        return "Die Worldborder entspricht dem XP-Level der Spieler. Skalierung im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a§lLevel = Border");
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
        CustomGUI gui = new CustomGUI(Component.text("§a§lBorder Multiplikator pro Level"), 3);

        double[] mults = {1.0, 2.0, 3.0, 5.0};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < mults.length; i++) {
            double m = mults[i];
            ItemStack item = createSettingsItem(
                    Material.EXPERIENCE_BOTTLE,
                    "§e§l" + m + " Blöcke pro Level",
                    "§7WorldBorder = Level * " + m + " Blöcke.",
                    "",
                    m == levelMultiplier ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                levelMultiplier = m;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                if (isEnabled()) updateBorder();
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
        highestLevelReached = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLevel() > highestLevelReached) {
                highestLevelReached = p.getLevel();
            }
        }

        updateBorder();

        World world = Bukkit.getWorlds().get(0);
        Location center = world.getWorldBorder().getCenter();
        Location targetLoc = world.getHighestBlockAt(center).getLocation().add(0.5, 1.0, 0.5);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) {
                p.teleport(targetLoc);
                p.sendMessage("§a[Level = Border] Du wurdest in die Border-Zone teleportiert!");
            }
        }
    }

    @Override
    public void onDisable() {
        highestLevelReached = 0;
        for (World world : Bukkit.getWorlds()) {
            world.getWorldBorder().reset();
        }
        Bukkit.broadcastMessage("§a[Level = Border] WorldBorder wurde wieder auf den Standardwert zurückgesetzt.");
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        int newLevel = event.getNewLevel();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLevel() != newLevel) {
                p.setLevel(newLevel);
            }
        }

        if (newLevel > highestLevelReached) {
            highestLevelReached = newLevel;
            updateBorder();
        }
    }

    private void updateBorder() {
        double borderSize = Math.max(5.0, highestLevelReached * levelMultiplier);

        for (World world : Bukkit.getWorlds()) {
            WorldBorder border = world.getWorldBorder();
            border.setSize(borderSize);
        }

        Bukkit.broadcastMessage("§a[Level = Border] §eWorldBorder wurde auf §6" + borderSize + " §eBlöcke angepasst! (Level: " + highestLevelReached + ")");
    }

    @Override
    public Object getSettingsState() {
        return levelMultiplier;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            levelMultiplier = num.doubleValue();
        }
    }
}
