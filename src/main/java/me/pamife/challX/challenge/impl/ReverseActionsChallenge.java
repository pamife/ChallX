package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class ReverseActionsChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Alles rückgängig";
    }

    @Override
    public String getDescription() {
        return "Alle Aktionen (Blöcke abbauen/platzieren, Mobs töten) werden nach 4 Sekunden wieder rückgängig gemacht.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.REPEATING_COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§d§lAlles rückgängig");
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

        BlockState state = event.getBlock().getState();

        // Nach 80 Ticks (4 Sekunden) wiederherstellen
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isEnabled()) {
                    state.update(true, false);
                }
            }
        }.runTaskLater(ChallX.getInstance(), 80L);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        Block block = event.getBlock();
        BlockState originalState = event.getBlockReplacedState();

        // Nach 80 Ticks (4 Sekunden) gelöscht/zurückgesetzt
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isEnabled()) {
                    originalState.update(true, false);
                }
            }
        }.runTaskLater(ChallX.getInstance(), 80L);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return; // Spieler nicht respawnen
        if (entity.getKiller() == null) return; // Nur wenn von einem Spieler getötet

        Player killer = entity.getKiller();
        if (ChallX.getInstance().getSettingsManager().isExcluded(killer.getUniqueId())) return;

        EntityType type = event.getEntityType();
        Location loc = entity.getLocation();

        // Nach 80 Ticks (4 Sekunden) wiederbeleben
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isEnabled()) {
                    loc.getWorld().spawnEntity(loc, type);
                }
            }
        }.runTaskLater(ChallX.getInstance(), 80L);
    }
}
