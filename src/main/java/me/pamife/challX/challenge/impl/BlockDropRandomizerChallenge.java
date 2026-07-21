package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlockDropRandomizerChallenge extends BaseChallenge {

    private static final List<Material> RANDOM_ITEMS = Arrays.asList(
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
        return "Abgebaute Blöcke lassen einen zufälligen Gegenstand aus einem kuratierten Pool fallen.";
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        // Normalen Drop deaktivieren
        event.setDropItems(false);

        // Zufälliges Item droppen
        Location loc = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
        Material randomMaterial = RANDOM_ITEMS.get(new Random().nextInt(RANDOM_ITEMS.size()));
        loc.getWorld().dropItemNaturally(loc, new ItemStack(randomMaterial));
    }
}
