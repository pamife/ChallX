package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NoDuplicateItemsChallenge extends BaseChallenge {

    private BukkitTask task;

    @Override
    public String getName() {
        return "Keine doppelten Items";
    }

    @Override
    public String getDescription() {
        return "Spieler dürfen keine gleichen Items im Inventar haben wie andere Mitspieler.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eKeine doppelten Items");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        // Kontinuierliche Prüfung jede Sekunde
        task = new BukkitRunnable() {
            @Override
            public void run() {
                checkAllPlayers();
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

    private void checkAllPlayers() {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        // Ordnet jedem Material die Spieler zu, die es im Inventar haben
        Map<Material, Set<Player>> materialHolders = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;
            if (player.isDead()) continue;

            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null) continue;
                Material type = item.getType();
                if (type == Material.AIR) continue;
                if (item.getAmount() <= 0) continue;

                materialHolders.computeIfAbsent(type, k -> new HashSet<>()).add(player);
            }
        }

        // Spieler ermitteln, die Duplikate besitzen
        Set<Player> playersToDamage = new HashSet<>();
        Map<Player, Set<Material>> playerDuplicateMaterials = new HashMap<>();

        for (Map.Entry<Material, Set<Player>> entry : materialHolders.entrySet()) {
            Set<Player> holders = entry.getValue();
            if (holders.size() > 1) { // Mehr als 1 Spieler hat dieses Item!
                for (Player p : holders) {
                    playersToDamage.add(p);
                    playerDuplicateMaterials.computeIfAbsent(p, k -> new HashSet<>()).add(entry.getKey());
                }
            }
        }

        // Schaden zufügen und informieren
        for (Player p : playersToDamage) {
            p.damage(2.0); // 1 Herz Schaden
            Set<Material> duplicates = playerDuplicateMaterials.get(p);
            p.sendMessage("§cDu besitzt dieselben Items wie ein anderer Spieler: §e" + duplicates.toString());
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        }
    }

    // --- Events für unmittelbare Reaktion ---
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        Bukkit.getScheduler().runTask(ChallX.getInstance(), this::checkAllPlayers);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!isEnabled()) return;
        if (event.getEntity() instanceof Player) {
            Bukkit.getScheduler().runTask(ChallX.getInstance(), this::checkAllPlayers);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!isEnabled()) return;
        Bukkit.getScheduler().runTask(ChallX.getInstance(), this::checkAllPlayers);
    }
}
