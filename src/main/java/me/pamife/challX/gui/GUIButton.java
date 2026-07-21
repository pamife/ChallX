package me.pamife.challX.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.function.Consumer;

public class GUIButton {
    private final ItemStack item;
    private final Consumer<InventoryClickEvent> action;

    public GUIButton(ItemStack item, Consumer<InventoryClickEvent> action) {
        this.item = item;
        this.action = action;
    }

    public ItemStack getItem() {
        return item;
    }

    public Consumer<InventoryClickEvent> getAction() {
        return action;
    }
}
