package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NoCraftingChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Kein Craften";
    }

    @Override
    public String getDescription() {
        return "Spieler können keinen Crafting Table benutzen und nichts herstellen.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eKein Craften");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getWhoClicked() instanceof Player player) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            event.setCancelled(true);
            player.sendMessage("§cDu darfst nichts herstellen (Kein Craften ist aktiv)!");
        }
    }

    @EventHandler
    public void onOpenInventory(InventoryOpenEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getPlayer() instanceof Player player) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            if (event.getInventory().getType() == InventoryType.WORKBENCH) {
                event.setCancelled(true);
                player.sendMessage("§cDu darfst keinen Crafting Table benutzen!");
            }
        }
    }
}
