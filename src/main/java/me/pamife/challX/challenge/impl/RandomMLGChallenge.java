package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RandomMLGChallenge extends BaseChallenge {

    private BukkitTask task;
    private final Map<UUID, ItemStack> originalItems = new HashMap<>();
    private final List<Block> placedBlocks = new ArrayList<>();

    private int interval = 180; // Standard: 3 Minuten
    private int minHeight = 25;
    private int maxHeight = 65;

    private static final List<Material> MLG_ITEMS = Arrays.asList(
            Material.WATER_BUCKET,
            Material.COBWEB,
            Material.POWDER_SNOW_BUCKET,
            Material.SLIME_BLOCK,
            Material.HAY_BLOCK
    );

    @Override
    public String getName() {
        return "Random MLG";
    }

    @Override
    public String getDescription() {
        return "Teleportiert alle Spieler regelmäßig mit einem zufälligen MLG-Gegenstand in die Höhe.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.COBWEB);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bRandom MLG");
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
        CustomGUI gui = new CustomGUI(Component.text("§b§lRandom MLG Einstellungen"), 3);

        // Intervall (Slots 10, 11, 12)
        gui.setButton(10, new GUIButton(
                createSettingsItem(Material.RED_WOOL, "§c-10s Intervall", "§7Intervall verringern"),
                e -> {
                    if (interval > 10) {
                        interval -= 10;
                        restartTask();
                    }
                    openSettings(player);
                }
        ));
        gui.setButton(11, new GUIButton(
                createSettingsItem(Material.CLOCK, "§eIntervall: §6" + interval + "s", "§7Wie oft der MLG stattfindet.", "§7Aktuell: §a" + formatTime(interval)),
                e -> {}
        ));
        gui.setButton(12, new GUIButton(
                createSettingsItem(Material.GREEN_WOOL, "§a+10s Intervall", "§7Intervall erhöhen"),
                e -> {
                    interval += 10;
                    restartTask();
                    openSettings(player);
                }
        ));

        // Min-Höhe (Slots 13, 14, 15)
        gui.setButton(13, new GUIButton(
                createSettingsItem(Material.RED_WOOL, "§c-5 Blöcke Min-Höhe", "§7Minimale Höhe verringern"),
                e -> {
                    if (minHeight > 5) {
                        minHeight -= 5;
                    }
                    openSettings(player);
                }
        ));
        gui.setButton(14, new GUIButton(
                createSettingsItem(Material.SCAFFOLDING, "§eMin-Höhe: §6" + minHeight + " Blöcke", "§7Minimale Fallhöhe."),
                e -> {}
        ));
        gui.setButton(15, new GUIButton(
                createSettingsItem(Material.GREEN_WOOL, "§a+5 Blöcke Min-Höhe", "§7Minimale Höhe erhöhen"),
                e -> {
                    if (minHeight + 5 <= maxHeight) {
                        minHeight += 5;
                    }
                    openSettings(player);
                }
        ));

        // Max-Höhe (Slots 19, 20, 21)
        gui.setButton(19, new GUIButton(
                createSettingsItem(Material.RED_WOOL, "§c-5 Blöcke Max-Höhe", "§7Maximale Höhe verringern"),
                e -> {
                    if (maxHeight - 5 >= minHeight) {
                        maxHeight -= 5;
                    }
                    openSettings(player);
                }
        ));
        gui.setButton(20, new GUIButton(
                createSettingsItem(Material.DIRT, "§eMax-Höhe: §6" + maxHeight + " Blöcke", "§7Maximale Fallhöhe."),
                e -> {}
        ));
        gui.setButton(21, new GUIButton(
                createSettingsItem(Material.GREEN_WOOL, "§a+5 Blöcke Max-Höhe", "§7Maximale Höhe erhöhen"),
                e -> {
                    maxHeight += 5;
                    openSettings(player);
                }
        ));

        // Zurück (Slot 22)
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

    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        if (m > 0) {
            return m + "m " + s + "s";
        }
        return s + "s";
    }

    @Override
    public void onEnable() {
        restartTask();
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (UUID uuid : originalItems.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                int slot = p.getInventory().getHeldItemSlot();
                p.getInventory().setItem(slot, originalItems.get(uuid));
            }
        }
        originalItems.clear();

        for (Block b : placedBlocks) {
            b.setType(Material.AIR);
        }
        placedBlocks.clear();
    }

    private void restartTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (isEnabled()) {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                    Bukkit.broadcastMessage("§b[MLG] Mach dich bereit! Random MLG in 3 Sekunden...");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!isEnabled() || !ChallX.getInstance().getTimerManager().isRunning()) return;

                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                                if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;

                                triggerMLG(player);
                            }
                        }
                    }.runTaskLater(ChallX.getInstance(), 60L);
                }
            }.runTaskTimer(ChallX.getInstance(), interval * 20L, interval * 20L);
        }
    }

    private void triggerMLG(Player player) {
        UUID uuid = player.getUniqueId();
        int slot = player.getInventory().getHeldItemSlot();

        ItemStack currentItem = player.getInventory().getItem(slot);
        originalItems.put(uuid, currentItem != null ? currentItem.clone() : null);

        // Zufälliges MLG-Item auswählen
        Material mlgMaterial = MLG_ITEMS.get(new Random().nextInt(MLG_ITEMS.size()));
        player.getInventory().setItem(slot, new ItemStack(mlgMaterial));

        Location loc = player.getLocation();
        double height = minHeight + new Random().nextInt(Math.max(1, maxHeight - minHeight + 1));
        loc.setY(loc.getY() + height);
        player.teleport(loc);

        String itemName = switch (mlgMaterial) {
            case WATER_BUCKET -> "Wassereimer";
            case COBWEB -> "Spinnwebe";
            case POWDER_SNOW_BUCKET -> "Pulverschnee-Eimer";
            case SLIME_BLOCK -> "Schleimblock";
            case HAY_BLOCK -> "Heuballen";
            default -> mlgMaterial.name();
        };

        player.sendMessage("§b§lMLG! §eRette dich mit dem Item: §6§l" + itemName);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (originalItems.containsKey(uuid)) {
            if (player.isOnGround()) {
                ItemStack original = originalItems.remove(uuid);
                int slot = player.getInventory().getHeldItemSlot();

                // Inventar aufräumen
                for (Material mat : MLG_ITEMS) {
                    player.getInventory().remove(mat);
                }
                player.getInventory().remove(Material.BUCKET);

                player.getInventory().setItem(slot, original);
                player.sendMessage("§aMLG geschafft!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;

        if (originalItems.containsKey(player.getUniqueId())) {
            Block block = event.getBlock();
            placedBlocks.add(block);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (block.getType() == Material.WATER || block.getType() == Material.POWDER_SNOW) {
                        block.setType(Material.AIR);
                    }
                    placedBlocks.remove(block);
                }
            }.runTaskLater(ChallX.getInstance(), 60L);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;

        if (originalItems.containsKey(player.getUniqueId())) {
            Material type = event.getBlockPlaced().getType();
            if (type == Material.COBWEB || type == Material.SLIME_BLOCK || type == Material.HAY_BLOCK) {
                Block block = event.getBlockPlaced();
                placedBlocks.add(block);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (block.getType() == type) {
                            block.setType(Material.AIR);
                        }
                        placedBlocks.remove(block);
                    }
                }.runTaskLater(ChallX.getInstance(), 60L);
            }
        }
    }

    @Override
    public Object getSettingsState() {
        Map<String, Object> state = new HashMap<>();
        state.put("interval", interval);
        state.put("minHeight", minHeight);
        state.put("maxHeight", maxHeight);
        return state;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Map<?, ?> map) {
            Object intervalVal = map.get("interval");
            if (intervalVal instanceof Number num) {
                this.interval = num.intValue();
            }
            Object minVal = map.get("minHeight");
            if (minVal instanceof Number num) {
                this.minHeight = num.intValue();
            }
            Object maxVal = map.get("maxHeight");
            if (maxVal instanceof Number num) {
                this.maxHeight = num.intValue();
            }
            restartTask();
        }
    }
}
