package me.pamife.challX.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.Challenge;
import me.pamife.challX.setting.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CacheManager {
    private final File cacheFile;
    private final Gson gson;

    public CacheManager() {
        this.cacheFile = new File(ChallX.getInstance().getDataFolder(), ".cache.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void saveCache() {
        try {
            if (!ChallX.getInstance().getDataFolder().exists()) {
                ChallX.getInstance().getDataFolder().mkdirs();
            }

            CacheData data = new CacheData();

            // Theme
            data.selectedTheme = ChallX.getInstance().getThemeManager().getCurrentTheme().name();

            // Timer
            TimerManager tm = ChallX.getInstance().getTimerManager();
            data.time = tm.getTime();
            data.timerRunning = tm.isRunning();
            data.timerReverse = tm.isReverse();

            // Global Settings
            SettingsManager sm = ChallX.getInstance().getSettingsManager();
            for (Map.Entry<Setting, Boolean> entry : sm.getSettingStates().entrySet()) {
                data.settings.put(entry.getKey().name(), entry.getValue());
            }
            for (Map.Entry<Setting, Integer> entry : sm.getIntSettings().entrySet()) {
                data.intSettings.put(entry.getKey().name(), entry.getValue());
            }

            // Excluded Players
            for (UUID uuid : sm.getExcludedPlayers()) {
                data.excludedPlayers.add(uuid.toString());
            }

            // Killed Mobs
            ProjectManager pm = ChallX.getInstance().getProjectManager();
            data.allMobsProjectEnabled = pm.isEnabled();
            for (EntityType type : pm.getKilledMobs()) {
                data.killedMobs.add(type.name());
            }

            // Positions
            PositionManager posM = ChallX.getInstance().getPositionManager();
            for (Map.Entry<String, Location> entry : posM.getPositions().entrySet()) {
                Location loc = entry.getValue();
                if (loc != null && loc.getWorld() != null) {
                    CachedLocation cl = new CachedLocation();
                    cl.world = loc.getWorld().getName();
                    cl.x = loc.getX();
                    cl.y = loc.getY();
                    cl.z = loc.getZ();
                    cl.yaw = loc.getYaw();
                    cl.pitch = loc.getPitch();
                    data.positions.put(entry.getKey(), cl);
                }
            }

            // Challenges
            ChallengeManager cm = ChallX.getInstance().getChallengeManager();
            for (Challenge c : cm.getChallenges()) {
                if (c.isEnabled()) {
                    data.enabledChallenges.add(c.getClass().getSimpleName());
                }
                Object state = c.getSettingsState();
                if (state != null) {
                    data.challengeSettings.put(c.getClass().getSimpleName(), state);
                }
            }

            try (FileWriter writer = new FileWriter(cacheFile)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            ChallX.getInstance().getLogger().severe("Konnte Cache-Datei nicht speichern: " + e.getMessage());
        }
    }

    public void loadCache() {
        if (!cacheFile.exists()) return;

        try (FileReader reader = new FileReader(cacheFile)) {
            CacheData data = gson.fromJson(reader, CacheData.class);
            if (data == null) return;

            // Theme
            if (data.selectedTheme != null) {
                try {
                    ChallX.getInstance().getThemeManager().setCurrentTheme(ThemeManager.Theme.valueOf(data.selectedTheme));
                } catch (Exception ignored) {}
            }

            // Timer
            TimerManager tm = ChallX.getInstance().getTimerManager();
            tm.setTime(data.time);
            tm.setRunning(data.timerRunning);
            tm.setReverse(data.timerReverse);

            // Global Settings
            SettingsManager sm = ChallX.getInstance().getSettingsManager();
            for (Map.Entry<String, Boolean> entry : data.settings.entrySet()) {
                try {
                    Setting setting = Setting.valueOf(entry.getKey());
                    sm.setSetting(setting, entry.getValue());
                } catch (IllegalArgumentException ignored) {}
            }
            for (Map.Entry<String, Integer> entry : data.intSettings.entrySet()) {
                try {
                    Setting setting = Setting.valueOf(entry.getKey());
                    sm.setIntSetting(setting, entry.getValue());
                } catch (IllegalArgumentException ignored) {}
            }

            // Excluded Players
            for (String uuidStr : data.excludedPlayers) {
                try {
                    sm.excludePlayer(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException ignored) {}
            }

            // Killed Mobs
            ProjectManager pm = ChallX.getInstance().getProjectManager();
            pm.setEnabled(data.allMobsProjectEnabled);
            pm.reset();
            for (String mobName : data.killedMobs) {
                try {
                    pm.registerKill(EntityType.valueOf(mobName));
                } catch (IllegalArgumentException ignored) {}
            }

            // Positions
            PositionManager posM = ChallX.getInstance().getPositionManager();
            posM.clear();
            for (Map.Entry<String, CachedLocation> entry : data.positions.entrySet()) {
                CachedLocation cl = entry.getValue();
                World world = Bukkit.getWorld(cl.world);
                if (world != null) {
                    Location loc = new Location(world, cl.x, cl.y, cl.z, cl.yaw, cl.pitch);
                    posM.savePosition(entry.getKey(), loc);
                }
            }

            // Challenges
            ChallengeManager cm = ChallX.getInstance().getChallengeManager();
            for (Challenge c : cm.getChallenges()) {
                String key = c.getClass().getSimpleName();
                
                // Settings laden
                if (data.challengeSettings.containsKey(key)) {
                    c.loadSettingsState(data.challengeSettings.get(key));
                }
                
                // Status setzen
                if (data.enabledChallenges.contains(key)) {
                    c.setEnabled(true);
                } else {
                    c.setEnabled(false);
                }
            }
        } catch (IOException e) {
            ChallX.getInstance().getLogger().severe("Konnte Cache-Datei nicht laden: " + e.getMessage());
        }
    }

    private static class CacheData {
        String selectedTheme = "ZICKZACK_V5";
        int time = 0;
        boolean timerRunning = false;
        boolean timerReverse = false;
        Map<String, Boolean> settings = new HashMap<>();
        Map<String, Integer> intSettings = new HashMap<>();
        List<String> excludedPlayers = new ArrayList<>();
        List<String> killedMobs = new ArrayList<>();
        boolean allMobsProjectEnabled = false;
        String currentForceMobName = null;
        int forceMobTimeLeft = 300;
        Map<String, CachedLocation> positions = new HashMap<>();
        List<String> enabledChallenges = new ArrayList<>();
        Map<String, Object> challengeSettings = new HashMap<>();
    }

    private static class CachedLocation {
        String world;
        double x;
        double y;
        double z;
        float yaw;
        float pitch;
    }
}
