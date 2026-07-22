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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;

public class BedrockWallChallenge extends BaseChallenge {

    private BukkitTask task;
    private double wallX = 0.0;
    private double wallZ = 0.0;
    private boolean initialized = false;
    
    private double stepSpeed = 0.35; // Default 0.35 Blöcke pro halbe Sekunde (0.7m/s)

    @Override
    public String getName() {
        return "Bedrock-Wand";
    }

    @Override
    public String getDescription() {
        return "Eine 1x1 Bedrock-Säule verfolgt dich. Geschwindigkeit im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.BEDROCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§8§lBedrock-Wand");
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
        CustomGUI gui = new CustomGUI(Component.text("§8§lBedrock-Säule Tempo"), 3);

        double[] speeds = {0.15, 0.35, 0.60, 1.00};
        String[] names = {"§aLangsam (0.3m/s)", "§eNormal (0.7m/s)", "§cSchnell (1.2m/s)", "§4Extreme (2.0m/s)"};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < speeds.length; i++) {
            double s = speeds[i];
            ItemStack item = createSettingsItem(
                    Material.COMPASS,
                    names[i],
                    "§7Schrittweite: " + (s * 2) + " Blöcke/Sekunde",
                    "",
                    s == stepSpeed ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                stepSpeed = s;
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
        initialized = false;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                Player target = null;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
                    if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;
                    target = p;
                    break;
                }

                if (target == null) return;

                Location pLoc = target.getLocation();

                if (!initialized) {
                    wallX = pLoc.getX() - 15.0;
                    wallZ = pLoc.getZ() - 15.0;
                    initialized = true;
                    Bukkit.broadcastMessage("§8[Bedrock-Säule] §eDie Säule hat sich formiert und verfolgt dich!");
                }

                // Berechne Vektor zum Spieler
                double dx = pLoc.getX() - wallX;
                double dz = pLoc.getZ() - wallZ;
                double distance = Math.sqrt(dx * dx + dz * dz);

                if (distance > 0.1) {
                    double step = Math.min(stepSpeed, distance);
                    wallX += (dx / distance) * step;
                    wallZ += (dz / distance) * step;
                }

                int newX = (int) Math.floor(wallX);
                int newZ = (int) Math.floor(wallZ);
                int currentY = pLoc.getBlockY();

                for (int y = currentY - 5; y < currentY + 20; y++) {
                    Block b = target.getWorld().getBlockAt(newX, y, newZ);
                    if (b.getType() == Material.AIR || b.getType().isBurnable() || b.getType() == Material.GRASS_BLOCK || b.getType() == Material.DIRT || b.getType() == Material.STONE) {
                        b.setType(Material.BEDROCK);
                    }
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
        initialized = false;
    }

    @Override
    public Object getSettingsState() {
        return stepSpeed;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            stepSpeed = num.doubleValue();
        }
    }
}
