package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlockBreakSpawnMobChallenge extends BaseChallenge {

    private int spawnChancePercent = 50; // Default 50%

    private static final List<EntityType> MOBS = Arrays.asList(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER,
            EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.WITCH, EntityType.ENDERMAN
    );

    @Override
    public String getName() {
        return "Blockabbau = Mob";
    }

    @Override
    public String getDescription() {
        return "Beim Abbauen von Blöcken besteht die Chance, dass ein Mob spawnt. Chance im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lBlockabbau = Mob");
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
        CustomGUI gui = new CustomGUI(Component.text("§c§lSpawn-Wahrscheinlichkeit"), 3);

        int[] chances = {10, 25, 50, 100};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < chances.length; i++) {
            int c = chances[i];
            ItemStack item = createSettingsItem(
                    Material.SPAWNER,
                    "§e§l" + c + "% Chance",
                    "§7Bei Blockabbau " + c + "% Mob-Spawn.",
                    "",
                    c == spawnChancePercent ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                spawnChancePercent = c;
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
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        Random random = new Random();
        if (random.nextInt(100) < spawnChancePercent) {
            EntityType mob = MOBS.get(random.nextInt(MOBS.size()));
            event.getBlock().getWorld().spawnEntity(event.getBlock().getLocation().add(0.5, 0.5, 0.5), mob);
        }
    }

    @Override
    public Object getSettingsState() {
        return spawnChancePercent;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            spawnChancePercent = num.intValue();
        }
    }
}
