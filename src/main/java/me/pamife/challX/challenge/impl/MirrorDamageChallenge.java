package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class MirrorDamageChallenge extends BaseChallenge {

    private double mirrorChance = 0.25; // 25% Standard-Wahrscheinlichkeit

    @Override
    public String getName() {
        return "Gespiegelter Schaden";
    }

    @Override
    public String getDescription() {
        return "Schaden an Mobs wird mit einer gewissen Chance gespiegelt.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cGespiegelter Schaden");
            meta.setLore(Collections.singletonList("§7Wahrscheinlichkeit: §e" + (int)(mirrorChance * 100) + "%"));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public void openSettings(Player player) {
        CustomGUI gui = new CustomGUI(Component.text("§cSpiegel-Wahrscheinlichkeit"), 1);

        // Minus-Button (Slot 2)
        ItemStack minus = new ItemStack(Material.RED_WOOL);
        ItemMeta minusMeta = minus.getItemMeta();
        if (minusMeta != null) {
            minusMeta.setDisplayName("§c-10% Wahrscheinlichkeit");
            minus.setItemMeta(minusMeta);
        }
        gui.setButton(2, new GUIButton(minus, e -> {
            mirrorChance = Math.max(0.0, mirrorChance - 0.1);
            openSettings(player); // GUI aktualisieren
        }));

        // Info-Button (Slot 4)
        ItemStack info = new ItemStack(Material.SUNFLOWER);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§eAktuelle Chance: §6" + (int)(mirrorChance * 100) + "%");
            info.setItemMeta(infoMeta);
        }
        gui.setButton(4, new GUIButton(info, e -> {}));

        // Plus-Button (Slot 6)
        ItemStack plus = new ItemStack(Material.GREEN_WOOL);
        ItemMeta plusMeta = plus.getItemMeta();
        if (plusMeta != null) {
            plusMeta.setDisplayName("§a+10% Wahrscheinlichkeit");
            plus.setItemMeta(plusMeta);
        }
        gui.setButton(6, new GUIButton(plus, e -> {
            mirrorChance = Math.min(1.0, mirrorChance + 0.1);
            openSettings(player); // GUI aktualisieren
        }));

        // Zurück-Button (Slot 8)
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§cZurück");
            back.setItemMeta(backMeta);
        }
        gui.setButton(8, new GUIButton(back, e -> {
            // Main settings GUI öffnen
            ChallX.getInstance().openSettingsGUI(player);
        }));

        gui.open(player);
    }

    @Override
    public Object getSettingsState() {
        return mirrorChance;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            mirrorChance = num.doubleValue();
        }
    }

    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getDamager() instanceof Player player) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            if (event.getEntity() instanceof LivingEntity) {
                // Mit Wahrscheinlichkeit spiegeln
                if (Math.random() < mirrorChance) {
                    double damage = event.getDamage();
                    event.setCancelled(true); // Verhindert Schaden am Mob
                    player.damage(damage);
                    player.sendMessage("§cDein Schaden wurde gespiegelt! (§4-" + String.format("%.1f", damage) + " HP§c)");
                }
            }
        }
    }
}
