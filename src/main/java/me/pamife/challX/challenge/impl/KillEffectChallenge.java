package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class KillEffectChallenge extends BaseChallenge {

    private static final List<PotionEffectType> POSITIVE_EFFECTS = Arrays.asList(
            PotionEffectType.SPEED, PotionEffectType.STRENGTH, PotionEffectType.REGENERATION,
            PotionEffectType.HASTE, PotionEffectType.JUMP_BOOST, PotionEffectType.RESISTANCE,
            PotionEffectType.FIRE_RESISTANCE, PotionEffectType.NIGHT_VISION, PotionEffectType.WATER_BREATHING,
            PotionEffectType.ABSORPTION, PotionEffectType.SATURATION
    );

    @Override
    public String getName() {
        return "Kill = Effekt";
    }

    @Override
    public String getDescription() {
        return "Tötet ein Spieler ein Mob, erhält er einen zufälligen, permanenten Trank-Effekt.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.DRAGON_BREATH);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a§lKill = Effekt");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return;
        if (entity.getKiller() == null) return;

        Player killer = entity.getKiller();
        if (ChallX.getInstance().getSettingsManager().isExcluded(killer.getUniqueId())) return;
        if (killer.getGameMode() == org.bukkit.GameMode.SPECTATOR || killer.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        PotionEffectType effectType = POSITIVE_EFFECTS.get(new Random().nextInt(POSITIVE_EFFECTS.size()));
        
        // Permanenten Effekt (sehr lange Dauer) vergeben
        killer.addPotionEffect(new PotionEffect(effectType, PotionEffect.INFINITE_DURATION, 0, false, true, true));
        
        killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
        killer.sendMessage("§a[Kill = Effekt] Du hast den permanenten Effekt §e" + effectType.getName() + " §aerhalten!");
    }
}
