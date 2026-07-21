package me.pamife.challX.manager;

import org.bukkit.Location;
import java.util.*;

public class PositionManager {
    private final Map<String, Location> positions = new HashMap<>();

    public void savePosition(String name, Location location) {
        positions.put(name.toLowerCase(), location);
    }

    public Location getPosition(String name) {
        return positions.get(name.toLowerCase());
    }

    public boolean deletePosition(String name) {
        return positions.remove(name.toLowerCase()) != null;
    }

    public Map<String, Location> getPositions() {
        return positions;
    }

    public void clear() {
        positions.clear();
    }
}
