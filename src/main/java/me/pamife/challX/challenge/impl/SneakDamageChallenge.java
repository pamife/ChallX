package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SneakDamageChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Sneaken = Schaden";
    }

    @Override
    public String getDescription() {
        return "Jedes Mal, wenn ein Spieler sneakt, erhält er 1 Herz Schaden.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eSneaken = Schaden");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!isEnabled()) return;
        if (!event.isSneaking()) return; // Nur beim Ducken, nicht beim Aufstehen
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        player.damage(2.0); // 1 Herz Schaden
    }
}
