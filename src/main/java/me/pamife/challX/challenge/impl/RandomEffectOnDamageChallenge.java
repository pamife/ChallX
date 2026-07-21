package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomEffectOnDamageChallenge extends BaseChallenge {

    private static final List<PotionEffectType> EFFECTS = Arrays.asList(
            PotionEffectType.BLINDNESS,
            PotionEffectType.NAUSEA,
            PotionEffectType.HUNGER,
            PotionEffectType.POISON,
            PotionEffectType.SLOWNESS,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER,
            PotionEffectType.DARKNESS
    );

    @Override
    public String getName() {
        return "Effekt bei Schaden";
    }

    @Override
    public String getDescription() {
        return "Bei jedem erhaltenen Schaden erhält der Spieler einen zufälligen negativen Potion-Effekt.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§5Effekt bei Schaden");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (!(event.getEntity() instanceof Player player)) return;
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        // Zufälligen Effekt auswählen
        PotionEffectType randomEffect = EFFECTS.get(new Random().nextInt(EFFECTS.size()));
        player.addPotionEffect(new PotionEffect(randomEffect, 200, 0)); // 10 Sekunden (200 Ticks)
        player.sendMessage("§5[Schaden] §7Du hast den Effekt §d" + randomEffect.getKey().getKey().toUpperCase() + " §7erhalten!");
    }
}
