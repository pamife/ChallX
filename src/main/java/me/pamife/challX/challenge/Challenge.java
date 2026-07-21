package me.pamife.challX.challenge;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Challenge {
    String getName();
    String getDescription();
    ItemStack getIcon();
    
    boolean isEnabled();
    void setEnabled(boolean state);
    
    void onEnable();
    void onDisable();
    
    default boolean hasSettings() {
        return false;
    }
    
    default void openSettings(Player player) {
        // Standardmäßig keine Einstellungen
    }

    default Object getSettingsState() {
        return null;
    }

    default void loadSettingsState(Object state) {
        // Standardmäßig keine Einstellungen laden
    }
}
