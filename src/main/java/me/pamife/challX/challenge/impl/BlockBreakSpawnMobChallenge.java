package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Location;
import org.bukkit.Material;
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

    private static final List<EntityType> HOSTILE_MOBS = Arrays.asList(
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.CREEPER,
            EntityType.SPIDER,
            EntityType.CAVE_SPIDER,
            EntityType.WITCH,
            EntityType.SILVERFISH,
            EntityType.SLIME
    );

    @Override
    public String getName() {
        return "Block abbauen = Mob Spawn";
    }

    @Override
    public String getDescription() {
        return "Bei jedem abgebauten Block spawnt ein zufälliger feindlicher Mob.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cBlock abbauen = Mob Spawn");
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

        Location spawnLoc = event.getBlock().getLocation().add(0.5, 0.0, 0.5);
        EntityType randomMob = HOSTILE_MOBS.get(new Random().nextInt(HOSTILE_MOBS.size()));
        spawnLoc.getWorld().spawnEntity(spawnLoc, randomMob);
    }
}
