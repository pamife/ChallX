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
    
    // Verhindert, dass Items doppelt vergeben werden
    private final Set<Material> assignedItems = new HashSet<>();

    // Pool aller verfügbaren, echten Minecraft-Items (ca. 1000+ Items in 1.21)
    private static final List<Material> ITEM_POOL = new ArrayList<>();

    static {
        for (Material mat : Material.values()) {
            if (mat.isItem() && !mat.isLegacy() && mat != Material.AIR) {
                ITEM_POOL.add(mat);
            }
        }
    }

    @Override
    public String getName() {
        return "Random Block-Drops";
    }

    @Override
    public String getDescription() {
        return "Jeder Block-Typ hat einen festen, einzigartigen Drop (z. B. Birke = Kohle). Jedes Item wird nur einmal vergeben.";
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
                    assignedItems.clear();
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
        
        Material dropMaterial = dropMap.get(originalMaterial);
        
        // Zuweisung holen oder neu erzeugen
        if (dropMaterial == null) {
            List<Material> available = new ArrayList<>(ITEM_POOL);
            available.removeAll(assignedItems);
            
            if (available.isEmpty()) {
                // Notfall-Reset falls über 1000 verschiedene Blöcke abgebaut wurden
                assignedItems.clear();
                available.addAll(ITEM_POOL);
            }

            dropMaterial = available.get(new Random().nextInt(available.size()));
            dropMap.put(originalMaterial, dropMaterial);
            assignedItems.add(dropMaterial);
        }

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
            assignedItems.clear();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                try {
                    Material key = Material.valueOf((String) entry.getKey());
                    Material val = Material.valueOf((String) entry.getValue());
                    dropMap.put(key, val);
                    assignedItems.add(val);
                } catch (Exception ignored) {}
            }
        }
    }
}
