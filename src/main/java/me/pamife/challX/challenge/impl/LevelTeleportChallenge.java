package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class LevelTeleportChallenge extends BaseChallenge {

    private int pearlsPerLevel = 16; // Default 16 Enderperlen

    @Override
    public String getName() {
        return "Level = Teleport";
    }

    @Override
    public String getDescription() {
        return "Fortbewegung ausschließlich per Enderperlen! Jedes XP-Level gewährt dir frische Enderperlen. Anzahl im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§5§lLevel = Teleport");
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
        CustomGUI gui = new CustomGUI(Component.text("§5§lPerlen pro Levelaufstieg"), 3);

        int[] pearls = {8, 16, 32};
        int[] slots = {11, 13, 15};

        for (int i = 0; i < pearls.length; i++) {
            int p = pearls[i];
            ItemStack item = createSettingsItem(
                    Material.ENDER_PEARL,
                    "§e§l+" + p + " Enderperlen pro Level",
                    "§7Gibt +" + p + " Perlen bei Levelaufstieg.",
                    "",
                    p == pearlsPerLevel ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                pearlsPerLevel = p;
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
    public void onLevelUp(PlayerLevelChangeEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getNewLevel() > event.getOldLevel()) {
            Player player = event.getPlayer();
            player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, pearlsPerLevel));
            player.sendMessage("§5[Level = Teleport] Levelup! Du hast +" + pearlsPerLevel + " Enderperlen erhalten.");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) return;

        if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            if (!player.isGliding()) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public Object getSettingsState() {
        return pearlsPerLevel;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            pearlsPerLevel = num.intValue();
        }
    }
}
