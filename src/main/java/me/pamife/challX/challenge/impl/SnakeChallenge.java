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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class SnakeChallenge extends BaseChallenge {

    private int trailSeconds = 8; // Default 8s, -1 = Verfällt NIE

    @Override
    public String getName() {
        return "Snake";
    }

    @Override
    public String getDescription() {
        return "Spieler ziehen eine tödliche Wollspur hinter sich her. Wer auf rote Wolle tritt, stirbt sofort!";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lSnake");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lSnake Spurdauer"), 3);

        int[] times = {4, 8, 15, 30, -1};
        String[] labels = {"4 Sekunden", "8 Sekunden", "15 Sekunden", "30 Sekunden", "§c§lVerfällt NIE (Permanent)"};
        int[] slots = {10, 11, 12, 13, 15};

        for (int i = 0; i < times.length; i++) {
            int t = times[i];
            ItemStack item = createSettingsItem(
                    t == -1 ? Material.BARRIER : Material.CLOCK,
                    labels[i],
                    t == -1 ? "§7Die rote Wolle bleibt für immer bestehen." : "§7Rote Wolle verfällt nach " + t + "s.",
                    "",
                    t == trailSeconds ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                trailSeconds = t;
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

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;

        Block standOn = to.getBlock().getRelative(BlockFace.DOWN);
        Material type = standOn.getType();

        if (type == Material.RED_WOOL) {
            player.setHealth(0.0); // Sofortiger Tod
            player.sendMessage("§cDu bist in eine tödliche Snake-Spur gelaufen!");
            return;
        }

        if (type != Material.AIR && type != Material.WATER && type != Material.LAVA && type != Material.BEDROCK) {
            BlockState originalState = standOn.getState();
            standOn.setType(Material.RED_WOOL);

            // Nur wenn trailSeconds != -1 ist, verfällt die Wolle
            if (trailSeconds > 0) {
                long ticks = trailSeconds * 20L;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (isEnabled()) {
                            originalState.update(true, false);
                        }
                    }
                }.runTaskLater(ChallX.getInstance(), ticks);
            }
        }
    }

    @Override
    public Object getSettingsState() {
        return trailSeconds;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            trailSeconds = num.intValue();
        }
    }
}
