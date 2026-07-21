package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageFreezeChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Damage Freeze";
    }

    @Override
    public String getDescription() {
        return "Erleiden Spieler Schaden, werden sie für 5 Sekunden komplett eingefroren.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ICE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lDamage Freeze");
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
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

            // Pulverschnee-Freeze & Effekte anwenden
            player.setFreezeTicks(140); // 7 Sekunden visueller Freeze
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 255, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 200, false, false, true));

            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
            player.sendMessage("§b[Damage Freeze] Du wurdest für 5 Sekunden eingefroren!");
        }
    }
}
