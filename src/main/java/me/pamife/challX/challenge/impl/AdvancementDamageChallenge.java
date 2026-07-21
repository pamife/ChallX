package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdvancementDamageChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Achievement = Schaden";
    }

    @Override
    public String getDescription() {
        return "Bei jedem erzielen Achievement erleidet der Spieler 5 Herzen Schaden.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bAchievement = Schaden");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        // Rezeptfreischaltungen ignorieren
        if (event.getAdvancement().getKey().getKey().startsWith("recipes/")) {
            return;
        }

        // 10.0 HP = 5 Herzen Schaden
        player.damage(10.0);
        Bukkit.broadcastMessage("§c[Achievement] §e" + player.getName() + " §7hat ein Achievement erzielt und §c5 Herzen §7Schaden erlitten!");
    }
}
