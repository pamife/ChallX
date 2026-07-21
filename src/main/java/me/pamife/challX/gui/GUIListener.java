package me.pamife.challX.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof CustomGUI gui) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            GUIButton button = gui.getButton(slot);
            if (button != null && button.getAction() != null) {
                button.getAction().accept(event);
            }
        }
    }
}
