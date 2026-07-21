package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class JumpMobChallenge extends BaseChallenge {

    private static final List<EntityType> MOBS = Arrays.asList(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER,
            EntityType.CAVE_SPIDER, EntityType.ENDERMAN, EntityType.WITCH, EntityType.PILLAGER,
            EntityType.VINDICATOR, EntityType.BLAZE, EntityType.WITHER_SKELETON, EntityType.SLIME
    );

    @Override
    public String getName() {
        return "Sprung = Mob";
    }

    @Override
    public String getDescription() {
        return "Bei jedem Sprung spawnt ein zufälliges feindliches Mob an deiner Position.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.RABBIT_FOOT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lSprung = Mob");
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onJump(PlayerStatisticIncrementEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;
        if (event.getStatistic() != Statistic.JUMP) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        EntityType randomMob = MOBS.get(new Random().nextInt(MOBS.size()));
        player.getWorld().spawnEntity(player.getLocation(), randomMob);
        
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
        player.sendMessage("§c[Sprung = Mob] Ein §e" + randomMob.name() + " §cist gespawnt!");
    }
}
