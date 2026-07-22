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
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class NoTradingChallenge extends BaseChallenge {

    private int penaltyMode = 0; // 0 = Nur Blockieren, 1 = + 1 Herz Schaden

    @Override
    public String getName() {
        return "Kein Traden";
    }

    @Override
    public String getDescription() {
        return "Das Traden mit Villagern ist verboten. Strafe im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lKein Traden");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lTrade Strafe"), 3);

        int[] modes = {0, 1};
        String[] labels = {"Nur Blockieren", "Blockieren + 1 Herz Schaden"};
        int[] slots = {11, 15};

        for (int i = 0; i < modes.length; i++) {
            int m = modes[i];
            ItemStack item = createSettingsItem(
                    Material.EMERALD,
                    "§e§l" + labels[i],
                    "§7Strafe bei Trade-Versuch: " + labels[i],
                    "",
                    m == penaltyMode ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                penaltyMode = m;
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
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getInventory().getType() == InventoryType.MERCHANT) {
            if (event.getPlayer() instanceof Player player) {
                if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
                if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

                event.setCancelled(true);
                player.sendMessage("§c[Kein Traden] Das Traden mit Villagern ist verboten!");

                if (penaltyMode == 1) player.damage(2.0);
            }
        }
    }

    @Override
    public Object getSettingsState() {
        return penaltyMode;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            penaltyMode = num.intValue();
        }
    }
}
