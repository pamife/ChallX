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
import org.bukkit.util.Vector;

import java.util.Arrays;

public class FlyOnDamageChallenge extends BaseChallenge {

    private double launchVelocity = 2.0; // Default 2.0

    @Override
    public String getName() {
        return "Schaden = Fliegen";
    }

    @Override
    public String getDescription() {
        return "Bei Schaden wirst du in die Luft katapultiert. Stärke im Menü einstellbar.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§f§lSchaden = Fliegen");
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
        CustomGUI gui = new CustomGUI(Component.text("§f§lKatapult Stärke"), 3);

        double[] vels = {1.0, 2.0, 3.0, 5.0};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < vels.length; i++) {
            double v = vels[i];
            ItemStack item = createSettingsItem(
                    Material.FEATHER,
                    "§e§lStärke " + v + "x",
                    "§7Katapultiert " + v + "x so hoch.",
                    "",
                    v == launchVelocity ? "§a§lAktuell Ausgewählt" : "§7[Klicke zum Auswählen]"
            );
            gui.setButton(slots[i], new GUIButton(item, e -> {
                launchVelocity = v;
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

            player.setVelocity(new Vector(0, launchVelocity, 0));
            player.sendMessage("§f[Schaden = Fliegen] WUUUUSCH!");
        }
    }

    @Override
    public Object getSettingsState() {
        return launchVelocity;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Number num) {
            launchVelocity = num.doubleValue();
        }
    }
}
