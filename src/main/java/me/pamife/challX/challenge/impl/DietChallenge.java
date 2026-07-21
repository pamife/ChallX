package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DietChallenge extends BaseChallenge {

    private final Map<UUID, Set<Material>> consumedFoods = new HashMap<>();

    @Override
    public String getName() {
        return "Diät";
    }

    @Override
    public String getDescription() {
        return "Jeder Nahrungstyp kann nur ein einziges Mal gegessen werden. Nutze /diet zum Einsehen.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.COOKIE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lDiät");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        consumedFoods.clear();
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        Material foodType = event.getItem().getType();

        // Überprüfen, ob es essbar ist
        if (!foodType.isEdible()) return;

        Set<Material> eaten = consumedFoods.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());

        if (eaten.contains(foodType)) {
            event.setCancelled(true);
            player.sendMessage("§c[Diät] Du hast §e" + foodType.name() + " §cbereits gegessen! Jedes Essen ist nur einmal erlaubt.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        } else {
            eaten.add(foodType);
            player.sendMessage("§a[Diät] Du hast §e" + foodType.name() + " §aerfolgreich gegessen! Du kannst diesen Typ nun nicht mehr essen.");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.5f, 1.0f);
        }
    }

    public void showEatenFoods(Player player) {
        Set<Material> eaten = consumedFoods.get(player.getUniqueId());
        if (eaten == null || eaten.isEmpty()) {
            player.sendMessage("§a[Diät] Du hast bisher noch keine Nahrungsmittel gegessen.");
        } else {
            player.sendMessage("§a[Diät] Deine bereits verzehrten Nahrungsmittel (" + eaten.size() + "):");
            for (Material mat : eaten) {
                player.sendMessage("§7- §e" + mat.name());
            }
        }
    }
}
