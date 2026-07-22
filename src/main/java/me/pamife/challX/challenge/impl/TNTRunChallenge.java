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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class TNTRunChallenge extends BaseChallenge {

    private double delaySeconds = 0.5; // Default 0.5s (10 Ticks)

    @Override
    public String getName() {
        return "TNT-Run";
    }

    @Override
    public String getDescription() {
        return "Die Blöcke unter den Spielern verschwinden nach kurzer Zeit. Verzögerung im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.TNT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lTNT-Run");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lTNT-Run Verzögerung"), 3);

        double[] delays = {0.2, 0.5, 1.0, 2.0};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < delays.length; i++) {
            double d = delays[i];
            ItemStack item = createSettingsItem(
                    Material.CLOCK,
                    "§e§l" + d + " Sekunde(n) Verzögerung",
                    "§7Block verschwindet nach " + d + "s.",
                    "",
                    d == delaySeconds ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                delaySeconds = d;
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

        Block standOn = to.getBlock().getRelative(BlockFace.DOWN);
        if (standOn.getType() == Material.AIR || standOn.getType() == Material.BEDROCK) return;

        Block targetBlock = standOn;
        long ticks = (long) (delaySeconds * 20.0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (isEnabled() && targetBlock.getType() != Material.AIR && targetBlock.getType() != Material.BEDROCK) {
                    targetBlock.setType(Material.AIR);
                }
            }
        }.runTaskLater(ChallX.getInstance(), ticks);
    }

    @Override
    public Object getSettingsState() {
        return delaySeconds;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            delaySeconds = num.doubleValue();
        }
    }
}
