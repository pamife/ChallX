package me.pamife.challX.manager;

import me.pamife.challX.ChallX;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.Color;
import java.util.Arrays;

public class ThemeManager {

    public enum Theme {
        CRAFT_ATTACK_13("CraftAttack 13 (ORI)", "#880e1d", "#ae1024", Material.REDSTONE_BLOCK, "Pulsierendes Dunkelrot/Burgund - BastiGHG CA13 Vibe."),
        ZICKZACK_V5("ZickZack v5", "#3b9217", "#52f60d", Material.SLIME_BALL, "Pulsierendes Neon-Grün - Der legendäre ZickZack v5 Style."),
        ZICKZACK_V5_2M("Golden Ruby (2M Special)", "#cf0815", "#ffbe16", Material.GOLD_BLOCK, "Ausbalanciertes Rubinrot & strahlendes Gold."),
        ZICKZACK_V4("ZickZack v4", "#757aef", "#f452ce", Material.AMETHYST_SHARD, "Pulsierendes Violett-Blau & Neon-Pink Style."),
        ZICKZACK_V4_BLACK("ZickZack v4 (1.5M Black Edition)", "#ececec", "#787878", Material.POLISHED_BLACKSTONE, "Strahlendes Weiß mit flackernden Grautönen."),
        
        // 7 Zusätzliche Farb-Themes
        OCEAN_CYBERPUNK("Ocean Cyberpunk", "#00f2fe", "#4facfe", Material.PRISMARINE_CRYSTALS, "Strahlendes Cyan & Electric Blue."),
        SUNSET_FLAME("Sunset Flame", "#ff0844", "#ffb199", Material.FIRE_CHARGE, "Leuchtendes Feuerrot & Koralle."),
        AMETHYST_VIOLET("Amethyst Violet", "#b224ef", "#7579ff", Material.AMETHYST_BLOCK, "Tiefes Purpur & Mystisches Violett."),
        EMERALD_FOREST("Emerald Forest", "#11998e", "#38ef7d", Material.EMERALD, "Edles Smaragdgrün & Frische Minze."),
        MIDNIGHT_OBSIDIAN("Midnight Obsidian", "#232526", "#414345", Material.OBSIDIAN, "Sleekes Dunkelgrau & Silber."),
        SOLAR_FLARE("Solar Flare", "#f857a6", "#ff5858", Material.MAGMA_BLOCK, "Neon Pink & Magmarot."),
        ICE_BLIZZARD("Ice Blizzard", "#e0c3fc", "#8ec5fc", Material.PACKED_ICE, "Eisweiß & Himmelsblau.");

        private final String displayName;
        private final String color1;
        private final String color2;
        private final Material iconMaterial;
        private final String description;

        Theme(String displayName, String color1, String color2, Material iconMaterial, String description) {
            this.displayName = displayName;
            this.color1 = color1;
            this.color2 = color2;
            this.iconMaterial = iconMaterial;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getColor1() { return color1; }
        public String getColor2() { return color2; }
        public Material getIconMaterial() { return iconMaterial; }
        public String getDescription() { return description; }
    }

    private Theme currentTheme = Theme.ZICKZACK_V5;

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(Theme theme) {
        this.currentTheme = theme;
    }

    /**
     * Erzeugt einen ultra-smoothen, pulsierenden Farbverlauf für die Timer-Actionbar.
     */
    public String formatTimer(String timeStr) {
        long timeMs = System.currentTimeMillis();
        double factor1 = (Math.sin(timeMs / 600.0) + 1.0) / 2.0; // Slow smooth sine wave
        double factor2 = (Math.sin((timeMs + 300) / 600.0) + 1.0) / 2.0;

        String hexA = interpolateHex(currentTheme.getColor1(), currentTheme.getColor2(), factor1);
        String hexB = interpolateHex(currentTheme.getColor1(), currentTheme.getColor2(), factor2);

        return "<gradient:" + hexA + ":" + hexB + "><bold>" + timeStr + "</bold></gradient>";
    }

    public String formatPrefix(String prefixName) {
        return "<color:" + currentTheme.getColor2() + "><bold>[" + prefixName + "]</bold></color> ";
    }

    private String interpolateHex(String hex1, String hex2, double factor) {
        try {
            Color c1 = Color.decode(hex1);
            Color c2 = Color.decode(hex2);

            int r = (int) Math.round(c1.getRed() + factor * (c2.getRed() - c1.getRed()));
            int g = (int) Math.round(c1.getGreen() + factor * (c2.getGreen() - c1.getGreen()));
            int b = (int) Math.round(c1.getBlue() + factor * (c2.getBlue() - c1.getBlue()));

            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return hex1;
        }
    }

    public void openThemeGUI(Player player) {
        CustomGUI gui = new CustomGUI(Component.text("§6§lSelect Plugin Theme"), 4);

        Theme[] themes = Theme.values();
        for (int i = 0; i < themes.length; i++) {
            Theme t = themes[i];
            boolean selected = (t == currentTheme);

            ItemStack item = new ItemStack(t.getIconMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e§l" + t.getDisplayName());
                meta.setLore(Arrays.asList(
                        "§7" + t.getDescription(),
                        "§7Startfarbe: " + t.getColor1() + " | Endfarbe: " + t.getColor2(),
                        "",
                        selected ? "§a§l[AKTUELLES THEME]" : "§7[Klicke zum Aktivieren]"
                ));
                item.setItemMeta(meta);
            }

            final Theme selectedTheme = t;
            gui.setButton(i, new GUIButton(item, e -> {
                setCurrentTheme(selectedTheme);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                player.sendMessage(MiniMessage.miniMessage().deserialize(formatPrefix("Theme") + "<green>Theme gewechselt auf: <bold>" + selectedTheme.getDisplayName() + "</bold>"));
                openThemeGUI(player);
            }));
        }

        gui.setButton(31, new GUIButton(
                createItem(Material.BARRIER, "§cZurück zum Hauptmenü"),
                e -> ChallX.getInstance().openSettingsGUI(player)
        ));

        fillBackground(gui);
        gui.open(player);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
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
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, "§7 ");
        for (int i = 0; i < 36; i++) {
            if (gui.getButton(i) == null) {
                gui.setButton(i, new GUIButton(filler, e -> {}));
            }
        }
    }
}
