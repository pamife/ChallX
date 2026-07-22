package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class JumpDamageChallenge extends BaseChallenge {

    private double damageAmount = 2.0; // Default 1 Herz (2.0 HP)

    @Override
    public String getName() {
        return "Sprung = Schaden";
    }

    @Override
    public String getDescription() {
        return "Bei jedem Sprung erhältst du Schaden. Stärke im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lSprung = Schaden");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lSprungschaden Stärke"), 3);

        double[] damages = {1.0, 2.0, 4.0, 10.0};
        String[] labels = {"0.5 Herzen", "1.0 Herz", "2.0 Herzen", "5.0 Herzen"};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < damages.length; i++) {
            double d = damages[i];
            ItemStack item = createSettingsItem(
                    Material.REDSTONE,
                    "§e§l" + labels[i],
                    "§7Schaden bei Sprung: " + (d / 2.0) + " ❤",
                    "",
                    d == damageAmount ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                damageAmount = d;
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
    public void onJump(PlayerStatisticIncrementEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;
        if (event.getStatistic() != Statistic.JUMP) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        player.damage(damageAmount);
        player.sendMessage("§c[Sprung = Schaden] Du hast §6" + (damageAmount / 2.0) + " ❤ §cSchaden durch einen Sprung erlitten!");
    }

    @Override
    public Object getSettingsState() {
        return damageAmount;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            damageAmount = num.doubleValue();
        }
    }
}
