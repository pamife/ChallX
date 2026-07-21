package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        // Eigene Plugin-GUIs ignorieren
        if (event.getInventory().getHolder() instanceof CustomGUI) return;

        if (!(event.getPlayer() instanceof Player player)) return;
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        // Inventar schließen und Schaden zufügen
        event.setCancelled(true);
        player.damage(2.0); // 1 Herz Schaden
        player.sendMessage("§cDu darfst dein Inventar / Container nicht öffnen!");
    }
}
