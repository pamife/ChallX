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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ItemDecayChallenge extends BaseChallenge {

    private BukkitTask task;
    private int intervalSeconds = 15; // Default 15s

    @Override
    public String getName() {
        return "Item Decay";
    }

    @Override
    public String getDescription() {
        return "Verfällt (löscht) ein zufälliges Item im Inventar in regelmäßigen Intervallen. Intervall einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ROTTEN_FLESH);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lItem Decay");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lItem Decay Intervall"), 3);

        int[] times = {5, 10, 15, 30};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < times.length; i++) {
            int t = times[i];
            ItemStack item = createSettingsItem(
                    Material.CLOCK,
                    "§e§lAlle " + t + " Sekunden",
                    "§7Löscht alle " + t + "s ein zufälliges Item.",
                    "",
                    t == intervalSeconds ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                intervalSeconds = t;
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
        long ticks = intervalSeconds * 20L;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                Random random = new Random();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                    if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) continue;

                    List<Integer> occupiedSlots = new ArrayList<>();
                    for (int i = 0; i < 36; i++) {
                        ItemStack item = player.getInventory().getItem(i);
                        if (item != null && item.getType() != Material.AIR) {
                            occupiedSlots.add(i);
                        }
                    }

                    if (!occupiedSlots.isEmpty()) {
                        int randomSlot = occupiedSlots.get(random.nextInt(occupiedSlots.size()));
                        ItemStack decayItem = player.getInventory().getItem(randomSlot);
                        
                        player.getInventory().setItem(randomSlot, null);
                        player.sendMessage("§c[Item Decay] Dein Item §e" + (decayItem != null ? decayItem.getType().name() : "Unbekannt") + " §cist verfallen!");
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), ticks, ticks);
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public Object getSettingsState() {
        return intervalSeconds;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            intervalSeconds = num.intValue();
        }
    }
}
