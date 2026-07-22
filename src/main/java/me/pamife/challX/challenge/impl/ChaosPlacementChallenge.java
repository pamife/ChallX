package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ChaosPlacementChallenge extends BaseChallenge {

    private int chaosChancePercent = 100; // Default 100%

    private static final List<Material> CHAOS_BLOCKS = new ArrayList<>();

    static {
        for (Material m : Material.values()) {
            if (m.isBlock() && !m.isLegacy() && m != Material.AIR && m != Material.BEDROCK) {
                CHAOS_BLOCKS.add(m);
            }
        }
    }

    @Override
    public String getName() {
        return "Chaos Platzierung";
    }

    @Override
    public String getDescription() {
        return "Beim Platzieren eines Blocks wird stattdessen ein zufälliger Block gesetzt. Wahrscheinlichkeit einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.SPONGE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§d§lChaos Platzierung");
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
        CustomGUI gui = new CustomGUI(Component.text("§d§lChaos Wahrscheinlichkeit"), 3);

        int[] chances = {25, 50, 75, 100};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < chances.length; i++) {
            int c = chances[i];
            ItemStack item = createSettingsItem(
                    Material.SPONGE,
                    "§e§l" + c + "% Chaos Chance",
                    "§7" + c + "% Chance auf Zufallsblock.",
                    "",
                    c == chaosChancePercent ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                chaosChancePercent = c;
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

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        Random random = new Random();
        if (random.nextInt(100) < chaosChancePercent) {
            Material randomMat = CHAOS_BLOCKS.get(random.nextInt(CHAOS_BLOCKS.size()));
            event.getBlockPlaced().setType(randomMat);
        }
    }

    @Override
    public Object getSettingsState() {
        return chaosChancePercent;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            chaosChancePercent = num.intValue();
        }
    }
}
