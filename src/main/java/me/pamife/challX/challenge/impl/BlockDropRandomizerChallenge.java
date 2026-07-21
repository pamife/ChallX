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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BlockDropRandomizerChallenge extends BaseChallenge {

    // Feste Zuweisung von abgebautem Block-Typ zu Drop-Typ
    private final Map<Material, Material> dropMap = new HashMap<>();

    private static final List<Material> CURATED_POOL = Arrays.asList(
            Material.DIRT, Material.COBBLESTONE, Material.OAK_LOG, Material.STICK, Material.WHEAT_SEEDS,
            Material.COAL, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND, Material.EMERALD,
            Material.NETHERITE_SCRAP, Material.NETHERITE_INGOT, Material.REDSTONE, Material.LAPIS_LAZULI, Material.OBSIDIAN,
            Material.APPLE, Material.BREAD, Material.COOKED_BEEF, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE,
            Material.WATER_BUCKET, Material.LAVA_BUCKET, Material.MILK_BUCKET, Material.IRON_SWORD, Material.DIAMOND_SWORD,
            Material.NETHERITE_SWORD, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE, Material.BOW,
            Material.ARROW, Material.TRIDENT, Material.CROSSBOW, Material.SHIELD, Material.IRON_HELMET,
            Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.ELYTRA, Material.TOTEM_OF_UNDYING, Material.SHULKER_BOX,
            Material.ENDER_PEARL, Material.SLIME_BALL, Material.STRING, Material.GUNPOWDER, Material.TNT
    );

    @Override
    public String getName() {
        return "Random Block-Drops";
    }

    @Override
    public String getDescription() {
        return "Jeder Block-Typ hat einen festen, zufälligen Drop (z. B. Birke = Kohle).";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.DISPENSER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bRandom Block-Drops");
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
        CustomGUI gui = new CustomGUI(Component.text("§b§lBlock-Drops Einstellungen"), 3);

        // Mischen-Button (Slot 13)
        gui.setButton(13, new GUIButton(
                createSettingsItem(Material.SPIDER_EYE, "§e§lDrops neu mischen", "§7Setzt alle aktuellen Zuweisungen zurück.", "§7Beim nächsten Abbauen wird neu ausgewürfelt."),
                e -> {
                    dropMap.clear();
                    Bukkit.broadcastMessage("§b[Randomizer] §eAlle Block-Drops wurden neu gemischt!");
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                    openSettings(player);
                }
        ));

        // Zurück-Button (Slot 22)
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
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        Material originalMaterial = event.getBlock().getType();
        
        // Zuweisung holen oder neu erzeugen
        Material dropMaterial = dropMap.computeIfAbsent(originalMaterial, k -> 
                CURATED_POOL.get(new Random().nextInt(CURATED_POOL.size()))
        );

        event.setDropItems(false);

        Location loc = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
        loc.getWorld().dropItemNaturally(loc, new ItemStack(dropMaterial));
    }

    @Override
    public Object getSettingsState() {
        // Wir speichern das Zuweisungs-Mapping
        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<Material, Material> entry : dropMap.entrySet()) {
            stringMap.put(entry.getKey().name(), entry.getValue().name());
        }
        return stringMap;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Map<?, ?> map) {
            dropMap.clear();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                try {
                    Material key = Material.valueOf((String) entry.getKey());
                    Material val = Material.valueOf((String) entry.getValue());
                    dropMap.put(key, val);
                } catch (Exception ignored) {}
            }
        }
    }
}
