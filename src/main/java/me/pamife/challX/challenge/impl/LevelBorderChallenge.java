package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LevelBorderChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "Level = Border";
    }

    @Override
    public String getDescription() {
        return "Die Worldborder entspricht dem XP-Level der Spieler. Beim Start werden alle Spieler in die Zone teleportiert.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(org.bukkit.Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a§lLevel = Border");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        updateBorder();

        // Berichtigung: Alle Spieler beim Start in das Zentrum der WorldBorder teleportieren
        World world = Bukkit.getWorlds().get(0);
        Location center = world.getWorldBorder().getCenter();
        Location targetLoc = world.getHighestBlockAt(center).getLocation().add(0.5, 1.0, 0.5);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) {
                p.teleport(targetLoc);
                p.sendMessage("§a[Level = Border] Du wurdest in die Border-Zone teleportiert!");
            }
        }
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        int newLevel = event.getNewLevel();

        // Level unter allen Online-Spielern synchronisieren
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLevel() != newLevel) {
                p.setLevel(newLevel);
            }
        }

        updateBorder();
    }

    private void updateBorder() {
        int maxLevel = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLevel() > maxLevel) {
                maxLevel = p.getLevel();
            }
        }

        // Mindestgröße 5.0, sonst Level * 2.0
        double borderSize = Math.max(5.0, maxLevel * 2.0);

        for (World world : Bukkit.getWorlds()) {
            WorldBorder border = world.getWorldBorder();
            border.setSize(borderSize);
        }

        Bukkit.broadcastMessage("§a[Level = Border] §eWorldBorder wurde auf §6" + borderSize + " §eBlöcke angepasst! (Level: " + maxLevel + ")");
    }
}
