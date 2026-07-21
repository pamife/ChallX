package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MobSwitchChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Mob Switch";
    }

    @Override
    public String getDescription() {
        return "Fügst du einem Mob Schaden zu, tauscht du sofort deine Position mit dem Mob.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§d§lMob Switch");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getDamager() instanceof Player player && event.getEntity() instanceof LivingEntity victim) {
            if (victim instanceof Player) return;
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

            Location pLoc = player.getLocation().clone();
            Location vLoc = victim.getLocation().clone();

            player.teleport(vLoc);
            victim.teleport(pLoc);

            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);
            player.sendMessage("§d[Mob Switch] Du hast die Position mit " + victim.getName() + " getauscht!");
        }
    }
}
