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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class NoDuplicateItemsChallenge extends BaseChallenge {

    private int penaltyMode = 0; // 0 = 1 Herz Schaden, 1 = 3 Herzen Schaden, 2 = Item Löschen

    @Override
    public String getName() {
        return "Keine gleichen Items";
    }

    @Override
    public String getDescription() {
        return "Du darfst keine doppelten Items im Inventar haben. Bestrafung im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lKeine gleichen Items");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lBestrafungs-Modus"), 3);

        int[] modes = {0, 1, 2};
        String[] labels = {"1 Herz Schaden", "3 Herzen Schaden", "Doppeltes Item Löschen"};
        int[] slots = {11, 13, 15};

        for (int i = 0; i < modes.length; i++) {
            int m = modes[i];
            ItemStack item = createSettingsItem(
                    Material.CHEST,
                    "§e§l" + labels[i],
                    "§7Bei doppeltem Item: " + labels[i],
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

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskTimer(ChallX.getInstance(), () -> {
            if (!isEnabled()) return;
            if (!ChallX.getInstance().getTimerManager().isRunning()) return;

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) continue;

                java.util.Set<Material> seen = new java.util.HashSet<>();
                for (int slot = 0; slot < 36; slot++) {
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (stack != null && stack.getType() != Material.AIR) {
                        if (seen.contains(stack.getType())) {
                            if (penaltyMode == 0) {
                                player.damage(2.0);
                                player.sendMessage("§c[Keine gleichen Items] Du hast doppelte Items: " + stack.getType().name() + "!");
                            } else if (penaltyMode == 1) {
                                player.damage(6.0);
                                player.sendMessage("§c[Keine gleichen Items] Du hast doppelte Items: " + stack.getType().name() + "!");
                            } else if (penaltyMode == 2) {
                                player.getInventory().setItem(slot, null);
                                player.sendMessage("§c[Keine gleichen Items] Doppeltes Item gelöscht: " + stack.getType().name() + "!");
                            }
                            break;
                        } else {
                            seen.add(stack.getType());
                        }
                    }
                }
            }
        }, 20L, 20L);
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
