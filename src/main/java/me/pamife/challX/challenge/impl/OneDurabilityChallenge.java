package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OneDurabilityChallenge extends BaseChallenge {

    @Override
    public String getName() {
        return "1 Haltbarkeit";
    }

    @Override
    public String getDescription() {
        return "Alle Werkzeuge und Waffen können nur ein einziges Mal verwendet werden und brechen danach sofort.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.WOODEN_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l1 Haltbarkeit");
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isTool(Material material) {
        String name = material.name();
        return name.endsWith("_SWORD") || name.endsWith("_PICKAXE") || name.endsWith("_AXE") || 
               name.endsWith("_SHOVEL") || name.endsWith("_HOE") || material == Material.BOW || 
               material == Material.CROSSBOW || material == Material.TRIDENT || material == Material.SHEARS || 
               material == Material.FLINT_AND_STEEL || material == Material.SHIELD;
    }

    private void breakTool(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR && isTool(item.getType())) {
            player.getInventory().setItemInMainHand(null);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage("§cDein Werkzeug ist zerbrochen!");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Player player = event.getPlayer();
        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        breakTool(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getDamager() instanceof Player player) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

            breakTool(player);
        }
    }
}
