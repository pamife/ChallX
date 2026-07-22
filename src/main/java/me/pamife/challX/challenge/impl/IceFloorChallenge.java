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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class IceFloorChallenge extends BaseChallenge {

    private final Set<UUID> activePlayers = new HashSet<>();
    private Material iceMaterial = Material.PACKED_ICE; // Default Packeis

    @Override
    public String getName() {
        return "Eisboden";
    }

    @Override
    public String getDescription() {
        return "Spieler ziehen einen Eisboden unter sich her, den sie mit Sneaken an/ausschalten. Eis-Typ im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.PACKED_ICE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lEisboden");
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
        CustomGUI gui = new CustomGUI(Component.text("§b§lEisboden Typ wählen"), 3);

        Material[] types = {Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE};
        String[] labels = {"Normales Eis", "Packeis", "Blaueis (Extrem Rutschig)"};
        int[] slots = {11, 13, 15};

        for (int i = 0; i < types.length; i++) {
            Material mat = types[i];
            ItemStack item = createSettingsItem(
                    mat,
                    "§e§l" + labels[i],
                    "§7Eisboden Typ: " + labels[i],
                    "",
                    mat == iceMaterial ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                iceMaterial = mat;
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
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) return;

        if (event.isSneaking()) {
            UUID uuid = player.getUniqueId();
            if (activePlayers.contains(uuid)) {
                activePlayers.remove(uuid);
                player.sendMessage("§b[Eisboden] §cDeaktiviert.");
            } else {
                activePlayers.add(uuid);
                player.sendMessage("§b[Eisboden] §aAktiviert.");
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (!activePlayers.contains(player.getUniqueId())) return;
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) return;

        Location to = event.getTo();
        if (to == null) return;

        Block standOn = to.getBlock().getRelative(BlockFace.DOWN);
        if (standOn.getType() != Material.AIR && standOn.getType() != Material.WATER && standOn.getType() != Material.LAVA && standOn.getType() != Material.BEDROCK) {
            standOn.setType(iceMaterial);
        }
    }

    @Override
    public Object getSettingsState() {
        return iceMaterial.name();
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof String str) {
            try {
                iceMaterial = Material.valueOf(str);
            } catch (Exception ignored) {}
        }
    }
}
