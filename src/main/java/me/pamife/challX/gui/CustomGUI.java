package me.pamife.challX.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CustomGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Map<Integer, GUIButton> buttons;

    public CustomGUI(Component title, int rows) {
        this.buttons = new HashMap<>();
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
    }

    public void setButton(int slot, GUIButton button) {
        buttons.put(slot, button);
        inventory.setItem(slot, button.getItem());
    }

    public void removeButton(int slot) {
        buttons.remove(slot);
        inventory.setItem(slot, null);
    }

    public GUIButton getButton(int slot) {
        return buttons.get(slot);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
