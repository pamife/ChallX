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

public class RandomDropChallenge extends BaseChallenge {

    private BukkitTask task;

    @Override
    public String getName() {
        return "Random Droppen";
    }

    @Override
    public String getDescription() {
        return "Alle 10 Sekunden verlierst/droppst du automatisch ein zufälliges Item aus deinem Inventar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lRandom Droppen");
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

                    // Sammle alle Plätze mit Inhalt (Slots 0-35)
                    List<Integer> slots = new ArrayList<>();
                    for (int i = 0; i < 36; i++) {
                        ItemStack item = player.getInventory().getItem(i);
                        if (item != null && item.getType() != Material.AIR) {
                            slots.add(i);
                        }
                    }

                    if (!slots.isEmpty()) {
                        int slot = slots.get(random.nextInt(slots.size()));
                        ItemStack item = player.getInventory().getItem(slot);

                        if (item != null) {
                            // Ein einzelnes Item droppen
                            ItemStack toDrop = item.clone();
                            toDrop.setAmount(1);
                            
                            // Im Inventar um 1 verringern
                            item.setAmount(item.getAmount() - 1);

                            player.getWorld().dropItemNaturally(player.getLocation(), toDrop);
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 0.5f);
                            player.sendMessage("§c[Random Drop] Du hast 1x §e" + toDrop.getType().name() + " §cfallen gelassen!");
                        }
                    }
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 200L, 200L); // Alle 10 Sekunden (200 Ticks)
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
