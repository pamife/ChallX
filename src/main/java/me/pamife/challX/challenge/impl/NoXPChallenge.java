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
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class NoXPChallenge extends BaseChallenge {

    private int penaltyMode = 0; // 0 = Sofort-Tod, 1 = 5 Herzen Schaden, 2 = XP auf 0 setzen

    @Override
    public String getName() {
        return "Keine XP";
    }

    @Override
    public String getDescription() {
        return "Erhalten Spieler XP, werden sie bestraft. Strafe im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lKeine XP");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lXP Strafe"), 3);

        int[] modes = {0, 1, 2};
        String[] labels = {"Sofort-Tod", "5 Herzen Schaden", "XP Nullen (Kein Tod)"};
        int[] slots = {11, 13, 15};

        for (int i = 0; i < modes.length; i++) {
            int m = modes[i];
            ItemStack item = createSettingsItem(
                    Material.EXPERIENCE_BOTTLE,
                    "§e§l" + labels[i],
                    "§7Strafe bei XP-Erhalt: " + labels[i],
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
    public void onExpChange(PlayerExpChangeEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getAmount() > 0) {
            Player player = event.getPlayer();
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

            if (penaltyMode == 0) {
                player.damage(20.0);
                player.sendMessage("§c[Keine XP] Du hast XP erhalten und bist gestorben!");
            } else if (penaltyMode == 1) {
                player.damage(10.0);
                player.sendMessage("§c[Keine XP] Du hast 5 Herzen Schaden durch XP-Erhalt bekommen!");
            } else if (penaltyMode == 2) {
                event.setAmount(0);
                player.sendMessage("§c[Keine XP] XP-Erhalt wurde genullt!");
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
