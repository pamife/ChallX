package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WalkingDamageChallenge extends BaseChallenge {

    private final Map<UUID, Double> distanceMap = new HashMap<>();
    private int distanceThreshold = 20; // Default 20 Blöcke

    @Override
    public String getName() {
        return "Laufen = Schaden";
    }

    @Override
    public String getDescription() {
        return "Nach einer bestimmten Anzahl gelaufener Blöcke erhältst du Schaden. Distanz im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lLaufen = Schaden");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lLaufschaden Distanz"), 3);

        int[] dists = {10, 20, 50, 100};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < dists.length; i++) {
            int d = dists[i];
            ItemStack item = createSettingsItem(
                    Material.COMPASS,
                    "§e§lAlle " + d + " Blöcke",
                    "§71 Herz Schaden alle " + d + " Blöcke.",
                    "",
                    d == distanceThreshold ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                distanceThreshold = d;
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
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        double dist = from.distance(to);
        UUID uuid = player.getUniqueId();
        double currentDist = distanceMap.getOrDefault(uuid, 0.0) + dist;

        if (currentDist >= distanceThreshold) {
            distanceMap.put(uuid, 0.0);
            player.damage(2.0);
            player.sendMessage("§c[Laufen = Schaden] Du bist " + distanceThreshold + " Blöcke gelaufen!");
        } else {
            distanceMap.put(uuid, currentDist);
        }
    }

    @Override
    public Object getSettingsState() {
        return distanceThreshold;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            distanceThreshold = num.intValue();
        }
    }
}
