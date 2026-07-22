package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;

public class ChunkEffectChallenge extends BaseChallenge {

    private BukkitTask task;

    private static final List<PotionEffectType> EFFECTS = Arrays.asList(
            PotionEffectType.SPEED, PotionEffectType.SLOWNESS, PotionEffectType.HASTE,
            PotionEffectType.MINING_FATIGUE, PotionEffectType.STRENGTH, PotionEffectType.JUMP_BOOST,
            PotionEffectType.REGENERATION, PotionEffectType.RESISTANCE, PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.WATER_BREATHING, PotionEffectType.INVISIBILITY, PotionEffectType.NIGHT_VISION,
            PotionEffectType.GLOWING, PotionEffectType.SLOW_FALLING
    );

    @Override
    public String getName() {
        return "Chunk = Effekt";
    }

    @Override
    public String getDescription() {
        return "Jeder Chunk besitzt einen eigenen festen Effekt, den du erhältst, solange du dich in ihm befindest.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lChunk = Effekt");
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

                    Chunk chunk = player.getLocation().getChunk();
                    int hash = Math.abs((chunk.getX() * 31 + chunk.getZ()) % EFFECTS.size());
                    PotionEffectType effectType = EFFECTS.get(hash);

                    // Effekt anwenden (60 Ticks = 3s Dauer, Partikel an)
                    player.addPotionEffect(new PotionEffect(effectType, 60, 1, false, true, true));
                    player.sendActionBar(Component.text("§b[Chunk-Effekt] §e" + effectType.getName()));
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
