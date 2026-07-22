package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MirrorDamageChallenge extends BaseChallenge {

    private int mirrorPercent = 100; // Default 100%

    @Override
    public String getName() {
        return "Geteilter Schaden (Mirror)";
    }

    @Override
    public String getDescription() {
        return "Erhält ein Spieler Schaden, bekommen alle anderen Spieler ebenfalls Schaden. Prozentsatz im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lGeteilter Schaden (Mirror)");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lMirror Schaden Prozentsatz"), 3);

        int[] percents = {25, 50, 75, 100};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < percents.length; i++) {
            int p = percents[i];
            ItemStack item = createSettingsItem(
                    Material.REDSTONE,
                    "§e§l" + p + "% Schaden übertragen",
                    "§7Überträgt " + p + "% des Schadens.",
                    "",
                    p == mirrorPercent ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                mirrorPercent = p;
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

        if (event.getEntity() instanceof Player victim) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(victim.getUniqueId())) return;
            if (victim.getGameMode() == org.bukkit.GameMode.SPECTATOR || victim.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

            double sharedDamage = event.getDamage() * (mirrorPercent / 100.0);

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getUniqueId().equals(victim.getUniqueId()) && !ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) {
                    if (p.getGameMode() != org.bukkit.GameMode.SPECTATOR && p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                        p.damage(sharedDamage);
                        p.sendMessage("§c[Shared Damage] " + victim.getName() + " hat Schaden erlitten (" + sharedDamage + " HP)!");
                    }
                }
            }
        }
    }

    @Override
    public Object getSettingsState() {
        return mirrorPercent;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            mirrorPercent = num.intValue();
        }
    }
}
