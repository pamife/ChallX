package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NoTradingChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Kein Traden";
    }

    @Override
    public String getDescription() {
        return "Spieler können nicht mehr mit Villagern oder Händlern handeln.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eKein Traden");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onOpenTrade(InventoryOpenEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getPlayer() instanceof Player player) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            if (event.getInventory().getType() == InventoryType.MERCHANT) {
                event.setCancelled(true);
                player.sendMessage("§cDu darfst nicht mit Händlern handeln!");
            }
        }
    }

    @EventHandler
    public void onInteractVillager(PlayerInteractEntityEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;

        EntityType type = event.getRightClicked().getType();
        if (type == EntityType.VILLAGER || type == EntityType.WANDERING_TRADER) {
            event.setCancelled(true);
            player.sendMessage("§cDu darfst nicht mit Dorfbewohnern interagieren!");
        }
    }
}
