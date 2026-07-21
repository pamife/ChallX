package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LevelTeleportChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Level = Teleport";
    }

    @Override
    public String getDescription() {
        return "Fortbewegung geht ausschließlich per Enderperle. Bei jedem Levelaufstieg erhältst du 16 Enderperlen.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§d§lLevel = Teleport");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) {
                p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 16));
                p.sendMessage("§d[Level = Teleport] Du hast 16 Starter-Enderperlen erhalten!");
            }
        }
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getNewLevel() > event.getOldLevel()) {
            Player player = event.getPlayer();
            player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 16));
            player.sendMessage("§d[Level = Teleport] Levelaufstieg! Du hast +16 Enderperlen erhalten.");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) return;

        // Nur horizontale Bewegungen sperren (Fallen / Perlen-Teleport ist erlaubt)
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            if (!player.isGliding()) {
                event.setCancelled(true);
            }
        }
    }
}
