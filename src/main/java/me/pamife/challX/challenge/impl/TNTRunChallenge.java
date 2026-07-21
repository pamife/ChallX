package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class TNTRunChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "TNT-Run";
    }

    @Override
    public String getDescription() {
        return "Die Blöcke unter den Spielern verschwinden nach 8 Ticks (0.4s).";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.TNT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cTNT-Run");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        Location to = event.getTo();
        if (to == null) return;

        // Block unter den Füßen bestimmen
        Block block = to.clone().subtract(0, 0.01, 0).getBlock();
        Material type = block.getType();

        // Wenn der Block solide ist und kein Luft/Lava/Bedrock
        if (type.isSolid() && type != Material.BEDROCK && type != Material.BARRIER && type != Material.LAVA) {
            final Block targetBlock = block;
            
            // Nach 8 Ticks (0.4 Sekunden) entfernen
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isEnabled() || !ChallX.getInstance().getTimerManager().isRunning()) return;
                    if (targetBlock.getType() != Material.AIR && targetBlock.getType() != Material.BEDROCK && targetBlock.getType() != Material.BARRIER) {
                        // Sound abspielen
                        targetBlock.getWorld().playSound(targetBlock.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 1.0f, 1.0f);
                        targetBlock.setType(Material.AIR);
                    }
                }
            }.runTaskLater(ChallX.getInstance(), 8L);
        }
    }
}
