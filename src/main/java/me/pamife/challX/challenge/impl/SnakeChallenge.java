package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class SnakeChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Snake";
    }

    @Override
    public String getDescription() {
        return "Spieler ziehen eine Spur aus Roter Wolle hinter sich her. Wer auf rote Wolle tritt, stirbt sofort.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lSnake");
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
        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        // Nur bei Blockwechsel prüfen
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;

        Block standOn = to.getBlock().getRelative(BlockFace.DOWN);
        Material type = standOn.getType();

        // Wenn er auf rote Wolle tritt -> Tod!
        if (type == Material.RED_WOOL) {
            player.damage(20.0);
            player.sendMessage("§cDu bist in eine Snake-Spur gelaufen!");
            return;
        }

        // Spur legen, wenn der Block solide und kein Luft/Portal/Flüssigkeit ist
        if (type != Material.AIR && type != Material.WATER && type != Material.LAVA && type != Material.BEDROCK) {
            BlockState originalState = standOn.getState();
            standOn.setType(Material.RED_WOOL);

            // Nach 8 Sekunden (160 Ticks) zurücksetzen
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (isEnabled()) {
                        originalState.update(true, false);
                    }
                }
            }.runTaskLater(ChallX.getInstance(), 160L);
        }
    }
}
