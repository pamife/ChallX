package me.pamife.challX.challenge;

import me.pamife.challX.ChallX;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class BaseChallenge implements Challenge, Listener {
    protected boolean enabled = false;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean state) {
        if (this.enabled == state) return;
        this.enabled = state;
        if (state) {
            onEnable();
            // Register this class as listener dynamically
            Bukkit.getPluginManager().registerEvents(this, ChallX.getInstance());
        } else {
            // Unregister all event handlers in this class dynamically
            HandlerList.unregisterAll(this);
            onDisable();
        }
    }

    @Override
    public void onEnable() {
        // Kann von Subklassen überschrieben werden
    }

    @Override
    public void onDisable() {
        // Kann von Subklassen überschrieben werden
    }
}
