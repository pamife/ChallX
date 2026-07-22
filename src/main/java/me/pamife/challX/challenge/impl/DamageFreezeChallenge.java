package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class DamageFreezeChallenge extends BaseChallenge {

    private int freezeSeconds = 5; // Default 5s

    @Override
    public String getName() {
        return "Damage Freeze";
    }

    @Override
    public String getDescription() {
        return "Erleiden Spieler Schaden, werden sie eingefroren. Dauer im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.ICE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lDamage Freeze");
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
        CustomGUI gui = new CustomGUI(Component.text("§b§lDamage Freeze Dauer"), 3);

        int[] times = {3, 5, 7, 10};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < times.length; i++) {
            int t = times[i];
            ItemStack item = createSettingsItem(
                    Material.CLOCK,
                    "§e§l" + t + " Sekunden Freeze",
                    "§7Friert den Spieler für " + t + "s ein.",
                    "",
                    t == freezeSeconds ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                freezeSeconds = t;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                openSettings(player);
            }));
        }

        gui.setButton(22, new GUIButton(
                createSettingsItem(Material.BARRIER, "§cZurück zu Challenges"),
                e -> ChallX.getInstance().openChallengesGUI(player)
        ));

        fillBackground(gui);
        gui.open(player);
    }

    private ItemStack createSettingsItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillBackground(CustomGUI gui) {
        ItemStack filler = createSettingsItem(Material.GRAY_STAINED_GLASS_PANE, "§7 ");
        for (int i = 0; i < 27; i++) {
            if (gui.getButton(i) == null) {
                gui.setButton(i, new GUIButton(filler, e -> {}));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        if (event.getEntity() instanceof Player player) {
            if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) return;
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

            int durationTicks = freezeSeconds * 20;
            player.setFreezeTicks(durationTicks + 40);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 255, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, durationTicks, 200, false, false, true));

            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
            player.sendMessage("§b[Damage Freeze] Du wurdest für " + freezeSeconds + " Sekunden eingefroren!");
        }
    }

    @Override
    public Object getSettingsState() {
        return freezeSeconds;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            freezeSeconds = num.intValue();
        }
    }
}
