package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NoXPChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Keine XP";
    }

    @Override
    public String getDescription() {
        return "Die Spieler sterben sofort, sobald sie Erfahrungspunkte (XP) erhalten.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aKeine XP");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        // Wenn die erhaltene XP positiv ist
        if (event.getAmount() > 0) {
            player.setHealth(0.0);
            Bukkit.broadcastMessage("§c" + player.getName() + " hat XP erhalten und ist gestorben!");
        }
    }
}
