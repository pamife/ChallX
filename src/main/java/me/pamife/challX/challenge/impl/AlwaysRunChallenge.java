package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AlwaysRunChallenge extends BaseChallenge {

    private BukkitTask task;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Integer> stillTicks = new HashMap<>();
    
    private int maxStillSeconds = 2; // Default 2s (20 Ticks)

    @Override
    public String getName() {
        return "Immer Laufen";
    }

    @Override
    public String getDescription() {
        return "Die Spieler können nie still stehen. Stillstehen fügt Schaden zu. Stillstandzeit einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a§lImmer Laufen");
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
        CustomGUI gui = new CustomGUI(Component.text("§a§lImmer Laufen Einstellungen"), 3);

        int[] times = {1, 2, 3, 5};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < times.length; i++) {
            int t = times[i];
            ItemStack item = createSettingsItem(
                    Material.CLOCK,
                    "§e§l" + t + " Sekunde(n) Stillstand",
                    "§7Maximal erlaubt: " + t + "s.",
                    "",
                    t == maxStillSeconds ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                maxStillSeconds = t;
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
        lastLocations.clear();
        stillTicks.clear();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                int maxTicksThreshold = maxStillSeconds * 2; // Task runs every 10 ticks (0.5s)

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                    if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) continue;

                    UUID uuid = player.getUniqueId();
                    Location loc = player.getLocation();
                    Location lastLoc = lastLocations.get(uuid);

                    if (lastLoc != null) {
                        if (lastLoc.getBlockX() == loc.getBlockX() &&
                            lastLoc.getBlockY() == loc.getBlockY() &&
                            lastLoc.getBlockZ() == loc.getBlockZ()) {
                            
                            int ticks = stillTicks.getOrDefault(uuid, 0) + 1;
                            stillTicks.put(uuid, ticks);

                            if (ticks >= maxTicksThreshold) {
                                player.damage(2.0); // 1 Herz Schaden
                                player.sendMessage("§cDu stehst zu lange still! Lauf weiter!");
                            }
                        } else {
                            stillTicks.put(uuid, 0);
                        }
                    }
                    lastLocations.put(uuid, loc);
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 10L, 10L);
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        lastLocations.clear();
        stillTicks.clear();
    }

    @Override
    public Object getSettingsState() {
        return maxStillSeconds;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            maxStillSeconds = num.intValue();
        }
    }
}
