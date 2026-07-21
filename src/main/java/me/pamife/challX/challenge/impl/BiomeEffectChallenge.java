package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;

public class BiomeEffectChallenge extends BaseChallenge {

    private BukkitTask task;

    private static final List<PotionEffectType> EFFECTS = Arrays.asList(
            PotionEffectType.SPEED, PotionEffectType.SLOWNESS, PotionEffectType.HASTE,
            PotionEffectType.MINING_FATIGUE, PotionEffectType.STRENGTH, PotionEffectType.JUMP_BOOST,
            PotionEffectType.REGENERATION, PotionEffectType.RESISTANCE, PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.WATER_BREATHING, PotionEffectType.NIGHT_VISION, PotionEffectType.GLOWING
    );

    @Override
    public String getName() {
        return "Biom = Effekt";
    }

    @Override
    public String getDescription() {
        return "Jedes Biom hat einen eigenen festen Effekt, den du erhältst, solange du dich darin aufhältst.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§g§lBiom = Effekt");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                    if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) continue;

                    Biome biome = player.getLocation().getBlock().getBiome();
                    int hash = Math.abs(biome.toString().hashCode() % EFFECTS.size());
                    PotionEffectType effectType = EFFECTS.get(hash);

                    player.addPotionEffect(new PotionEffect(effectType, 40, 0, false, false, true));
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L);
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
