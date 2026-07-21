package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OnlyDownChallenge extends BaseChallenge {

    private final Map<UUID, Double> minYMap = new HashMap<>();

    @Override
    public String getName() {
        return "Nur nach unten";
    }

    @Override
    public String getDescription() {
        return "Spieler können sich nur nach unten bewegen. Ein Aufsteigen der Y-Höhe ist verboten.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.POINTED_DRIPSTONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lNur nach unten");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        minYMap.clear();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) return;

        Location to = event.getTo();
        if (to == null) return;

        double currentY = to.getY();
        double minY = minYMap.getOrDefault(player.getUniqueId(), currentY);

        if (currentY > minY + 0.1) {
            event.setCancelled(true);
            player.sendMessage("§c[Nur nach unten] Du darfst dich nur nach unten bewegen!");
        } else if (currentY < minY) {
            minYMap.put(player.getUniqueId(), currentY);
        }
    }
}
