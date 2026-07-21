package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemDamageChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Item Schaden";
    }

    @Override
    public String getDescription() {
        return "Spieler erhalten Schaden, wenn sie Items aufheben oder im Inventar verschieben. (0.5 Herzen pro Item-Menge)";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.POISONOUS_POTATO);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lItem Schaden");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getEntity() instanceof Player player) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

            int amount = event.getItem().getItemStack().getAmount();
            double damage = amount * 1.0; // 0.5 Herzen = 1.0 Schaden pro Item

            player.damage(damage);
            player.sendMessage("§cDu hast §6" + (damage / 2.0) + " ❤ §cSchaden durch das Aufheben von §e" + amount + "x " + event.getItem().getItemStack().getType().name() + " §cerlitten!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        // Plugin-eigene Einstellungsmenüs ignorieren
        if (event.getInventory().getHolder() instanceof me.pamife.challX.gui.CustomGUI) return;

        if (event.getWhoClicked() instanceof Player player) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR) {
                int amount = clicked.getAmount();
                double damage = amount * 1.0;

                player.damage(damage);
                player.sendMessage("§cDu hast §6" + (damage / 2.0) + " ❤ §cSchaden durch das Bewegen von §e" + amount + "x " + clicked.getType().name() + " §cerlitten!");
            }
        }
    }
}
