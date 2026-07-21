package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OnlyMinecartChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Nur mit Minecart";
    }

    @Override
    public String getDescription() {
        return "Spieler können sich nur innerhalb eines Minecarts fortbewegen. Jeder erhält Starter-Schienen.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.MINECART);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lNur mit Minecart");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) {
                p.getInventory().addItem(new ItemStack(Material.MINECART, 1), new ItemStack(Material.RAIL, 64));
                p.sendMessage("§a[Nur mit Minecart] Du hast ein Minecart und Schienen erhalten!");
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        if (!player.isInsideVehicle() || !(player.getVehicle() instanceof Minecart)) {
            event.setCancelled(true);
            player.sendMessage("§c[Nur mit Minecart] Du kannst dich nur in einem Minecart bewegen!");
        }
    }
}
