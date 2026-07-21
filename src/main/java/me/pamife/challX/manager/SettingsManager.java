package me.pamife.challX.manager;

import me.pamife.challX.setting.Setting;
import java.util.*;

public class SettingsManager {
    private final Map<Setting, Boolean> settingStates = new HashMap<>();
    private final Set<UUID> excludedPlayers = new HashSet<>();

    public SettingsManager() {
        // Standardwerte initialisieren
        settingStates.put(Setting.SHARED_HEARTS, false);
        settingStates.put(Setting.ONE_LIFE_FOR_ALL, false);
        settingStates.put(Setting.NATURAL_REGEN, true);
        settingStates.put(Setting.CUT_CLEAN, false);
        settingStates.put(Setting.DAMAGE_IN_CHAT, false);
        settingStates.put(Setting.PVP, true);
    }

    public boolean getSetting(Setting setting) {
        return settingStates.getOrDefault(setting, false);
    }

    public void setSetting(Setting setting, boolean enabled) {
        settingStates.put(setting, enabled);
    }

    public Map<Setting, Boolean> getSettingStates() {
        return settingStates;
    }

    public void excludePlayer(UUID uuid) {
        excludedPlayers.add(uuid);
    }

    public void includePlayer(UUID uuid) {
        excludedPlayers.remove(uuid);
    }

    public boolean isExcluded(UUID uuid) {
        return excludedPlayers.contains(uuid);
    }

    public Set<UUID> getExcludedPlayers() {
        return excludedPlayers;
    }
}
