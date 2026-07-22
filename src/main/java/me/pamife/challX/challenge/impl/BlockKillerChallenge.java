package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BlockKillerChallenge extends BaseChallenge {

    private int radiusMode = 0; // 0 = Ganzer Chunk, 5 = Radius 5, 10 = Radius 10
    private final Map<UUID, Material> lastBlock = new HashMap<>();

    @Override
    public String getName() {
        return "Block Killer";
    }

    @Override
    public String getDescription() {
        return "Blöcke des Typs, auf dem du stehst, werden gelöscht. Bereich im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.TNT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lBlock Killer");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lBlock Killer Reichweite"), 3);

        int[] modes = {0, 5, 10};
        String[] labels = {"Ganzer Chunk (16x16)", "5 Blöcke Radius", "10 Blöcke Radius"};
        int[] slots = {11, 13, 15};

        for (int i = 0; i < modes.length; i++) {
            int m = modes[i];
            ItemStack item = createSettingsItem(
                    Material.TNT,
                    "§e§l" + labels[i],
                    "§7Löschbereich: " + labels[i],
                    "",
                    m == radiusMode ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                radiusMode = m;
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

        Location to = event.getTo();
        if (to == null) return;

        Block blockBelow = to.getBlock().getRelative(BlockFace.DOWN);
        Material type = blockBelow.getType();

        if (type == Material.AIR || type == Material.BEDROCK) return;

        Material prevType = lastBlock.get(player.getUniqueId());
        if (prevType == type) return;

        lastBlock.put(player.getUniqueId(), type);

        if (radiusMode == 0) {
            // Ganzer Chunk
            Chunk chunk = blockBelow.getChunk();
            World world = chunk.getWorld();
            int minY = world.getMinHeight();
            int maxY = world.getMaxHeight();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = minY; y < maxY; y++) {
                        Block b = chunk.getBlock(x, y, z);
                        if (b.getType() == type) {
                            b.setType(Material.AIR);
                        }
                    }
                }
            }
        } else {
            // Radius-Modus
            Location center = blockBelow.getLocation();
            World world = center.getWorld();
            int r = radiusMode;
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        Block b = world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                        if (b.getType() == type) {
                            b.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Object getSettingsState() {
        return radiusMode;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            radiusMode = num.intValue();
        }
    }
}
