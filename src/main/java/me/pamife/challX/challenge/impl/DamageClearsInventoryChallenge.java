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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DamageClearsInventoryChallenge extends BaseChallenge {

    private int clearMode = 0; // 0 = Alles, 1 = Haupthand, 2 = 5 Slots

    @Override
    public String getName() {
        return "Schaden leert Inventar";
    }

    @Override
    public String getDescription() {
        return "Das Inventar wird geleert, wenn der Spieler Schaden bekommt. Modus im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lSchaden leert Inventar");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lInventar-Lösch Modus"), 3);

        int[] modes = {0, 1, 2};
        String[] labels = {"Komplettes Inventar", "Nur Haupthand Item", "Zufällige 5 Slots"};
        int[] slots = {11, 13, 15};

        for (int i = 0; i < modes.length; i++) {
            int m = modes[i];
            ItemStack item = createSettingsItem(
                    Material.CHEST,
                    "§e§l" + labels[i],
                    "§7Bei Schaden: " + labels[i],
                    "",
                    m == clearMode ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                clearMode = m;
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
    public void onDamage(EntityDamageEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getEntity() instanceof Player player) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

            if (clearMode == 0) {
                player.getInventory().clear();
                player.sendMessage("§c[Schaden leert Inventar] Dein komplettes Inventar wurde geleert!");
            } else if (clearMode == 1) {
                player.getInventory().setItemInMainHand(null);
                player.sendMessage("§c[Schaden leert Inventar] Dein gehaltenes Item wurde geleert!");
            } else if (clearMode == 2) {
                Random random = new Random();
                List<Integer> slots = new ArrayList<>();
                for (int i = 0; i < 36; i++) {
                    if (player.getInventory().getItem(i) != null) slots.add(i);
                }
                for (int k = 0; k < 5 && !slots.isEmpty(); k++) {
                    int rIdx = random.nextInt(slots.size());
                    int s = slots.remove(rIdx);
                    player.getInventory().setItem(s, null);
                }
                player.sendMessage("§c[Schaden leert Inventar] 5 zufällige Slots wurden geleert!");
            }
        }
    }

    @Override
    public Object getSettingsState() {
        return clearMode;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            clearMode = num.intValue();
        }
    }
}
