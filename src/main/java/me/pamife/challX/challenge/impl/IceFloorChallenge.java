package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class IceFloorChallenge extends BaseChallenge {

    private final Set<UUID> iceActive = new HashSet<>();

    @Override
    public String getName() {
        return "Eisboden";
    }

    @Override
    public String getDescription() {
        return "Erzeugt Eis unter den Füßen. Sneaken schaltet es an/aus.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ICE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bEisboden");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onDisable() {
        iceActive.clear();
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;

        // Nur beim Drücken der Sneak-Taste toggeln
        if (event.isSneaking()) {
            UUID uuid = player.getUniqueId();
            if (iceActive.contains(uuid)) {
                iceActive.remove(uuid);
                player.sendMessage("§cEisboden deaktiviert!");
            } else {
                iceActive.add(uuid);
                player.sendMessage("§aEisboden aktiviert!");
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!iceActive.contains(uuid)) return;
        if (ChallX.getInstance().getSettingsManager().isExcluded(uuid)) return;

        if (event.getTo() == null) return;
        // Block direkt unter den Füßen bestimmen (am Zielort)
        Block blockUnder = event.getTo().clone().subtract(0, 0.01, 0).getBlock();
        Material originalType = blockUnder.getType();

        // Nur bei Luft oder Wasser Eis erzeugen
        if (originalType == Material.AIR || originalType == Material.WATER || originalType == Material.CAVE_AIR) {
            blockUnder.setType(Material.ICE);

            // Nach 3 Sekunden (60 Ticks) wieder zum Ursprungstyp zurücksetzen
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (blockUnder.getType() == Material.ICE) {
                        blockUnder.setType(originalType);
                    }
                }
            }.runTaskLater(ChallX.getInstance(), 60L);
        }
    }
}
