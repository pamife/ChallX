package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NoInventoryOpenChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Inventar öffnen = Schaden";
    }

    @Override
    public String getDescription() {
        return "Spieler dürfen kein Inventar (inklusive Kisten, Werkbänke und eigenes Inventar) öffnen, sonst erhalten sie Schaden.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.BARREL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cInventar öffnen = Schaden");
            item.setItemMeta(meta);
        }
        return item;
    }

    private void handleInventoryViolation(Player player) {
        player.damage(2.0); // 1 Herz Schaden
        player.sendMessage("§cDu darfst dein Inventar / Container nicht öffnen!");
        
        // 1 Tick verzögert das Inventar schließen, um Spigot-Bugs zu vermeiden
        Bukkit.getScheduler().runTask(ChallX.getInstance(), () -> player.closeInventory());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        // Eigene Plugin-GUIs ignorieren
        if (event.getInventory().getHolder() instanceof CustomGUI) return;

        if (!(event.getPlayer() instanceof Player player)) return;
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        // Container-Öffnung blockieren
        event.setCancelled(true);
        handleInventoryViolation(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        // Eigene Plugin-GUIs ignorieren
        if (event.getInventory().getHolder() instanceof CustomGUI) return;

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        // Interaktion im eigenen Inventar blockieren
        event.setCancelled(true);
        handleInventoryViolation(player);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        // Eigene Plugin-GUIs ignorieren
        if (event.getInventory().getHolder() instanceof CustomGUI) return;

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        // Dragging blockieren
        event.setCancelled(true);
        handleInventoryViolation(player);
    }
}
