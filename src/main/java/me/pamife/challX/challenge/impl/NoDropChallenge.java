package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NoDropChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Kein Droppen";
    }

    @Override
    public String getDescription() {
        return "Gegenstände können nicht aus dem Inventar weggeworfen werden.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.DROPPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lKein Droppen");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        event.setCancelled(true);
        player.sendMessage("§c[Kein Droppen] Du darfst keine Items wegwerfen!");
    }
}
