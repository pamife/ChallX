package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DamageClearsInventoryChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Schaden leert Inventar";
    }

    @Override
    public String getDescription() {
        return "Dein Inventar wird geleert, sobald du Schaden nimmst.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§4Schaden leert Inventar");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getEntity() instanceof Player player) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            
            player.getInventory().clear();
            player.sendMessage("§cDein Inventar wurde gelöscht, da du Schaden genommen hast!");
        }
    }
}
