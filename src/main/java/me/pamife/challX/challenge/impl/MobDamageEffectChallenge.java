package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MobDamageEffectChallenge extends BaseChallenge {

    private static final List<PotionEffectType> ALL_EFFECTS = Arrays.asList(
            PotionEffectType.SPEED, PotionEffectType.SLOWNESS, PotionEffectType.HASTE,
            PotionEffectType.MINING_FATIGUE, PotionEffectType.STRENGTH, PotionEffectType.JUMP_BOOST,
            PotionEffectType.REGENERATION, PotionEffectType.RESISTANCE, PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.BLINDNESS, PotionEffectType.HUNGER, PotionEffectType.WEAKNESS,
            PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.GLOWING, PotionEffectType.LEVITATION
    );

    @Override
    public String getName() {
        return "Mob Damage = Effekt";
    }

    @Override
    public String getDescription() {
        return "Erleidest du Schaden von einem Mob, erhältst du einen zufälligen permanenten Trank-Effekt.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.SPIDER_EYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lMob Damage = Effekt");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getEntity() instanceof Player player) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

            Entity damagerEntity = event.getDamager();
            LivingEntity attacker = null;

            if (damagerEntity instanceof LivingEntity living) {
                attacker = living;
            } else if (damagerEntity instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity shooter) {
                attacker = shooter;
            }

            if (attacker != null && !(attacker instanceof Player)) {
                PotionEffectType effectType = ALL_EFFECTS.get(new Random().nextInt(ALL_EFFECTS.size()));
                player.addPotionEffect(new PotionEffect(effectType, PotionEffect.INFINITE_DURATION, 0, false, true, true));

                player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 0.5f, 1.0f);
                player.sendMessage("§c[Mob Damage = Effekt] Du hast durch " + attacker.getType().name() + " den permanenten Effekt §e" + effectType.getName() + " §cerhalten!");
            }
        }
    }
}
