package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HealingDamageChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Heilen = Schaden";
    }

    @Override
    public String getDescription() {
        return "Tauscht Heilung gegen Schaden aus. Jeder Heilungseffekt fügt stattdessen Schaden zu.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.GLISTERING_MELON_SLICE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§dHeilen = Schaden");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (!(event.getEntity() instanceof Player player)) return;
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        double amount = event.getAmount();

        // Heilung abbrechen
        event.setCancelled(true);

        // Stattdessen Schaden zufügen
        player.damage(amount);
        player.sendMessage("§cDu hast §6" + String.format("%.1f", amount) + " HP §cSchaden durch Heilung erlitten!");
    }
}
