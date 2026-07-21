package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemDecayChallenge extends BaseChallenge {

    private BukkitTask task;

    @Override
    public String getName() {
        return "Item Decay";
    }

    @Override
    public String getDescription() {
        return "Alle 15 Sekunden verfällt (wird gelöscht) ein zufälliger Gegenstand in deinem Inventar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ROTTEN_FLESH);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lItem Decay");
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

                Random random = new Random();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                    if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) continue;

                    // Belegte Slots sammeln (Hauptinventar, Index 0-35)
                    List<Integer> occupiedSlots = new ArrayList<>();
                    for (int i = 0; i < 36; i++) {
                        ItemStack item = player.getInventory().getItem(i);
                        if (item != null && item.getType() != Material.AIR) {
                            occupiedSlots.add(i);
                        }
                    }

                    if (!occupiedSlots.isEmpty()) {
                        int randomSlot = occupiedSlots.get(random.nextInt(occupiedSlots.size()));
                        ItemStack decayItem = player.getInventory().getItem(randomSlot);
                        
                        player.getInventory().setItem(randomSlot, null);
                        player.sendMessage("§c[Item Decay] Dein Item §e" + (decayItem != null ? decayItem.getType().name() : "Unbekannt") + " §cist verfallen!");
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 300L, 300L); // Alle 15 Sekunden (300 Ticks)
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
