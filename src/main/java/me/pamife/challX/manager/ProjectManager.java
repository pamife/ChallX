package me.pamife.challX.manager;

import org.bukkit.entity.EntityType;
import java.util.*;

public class ProjectManager {
    private final Set<EntityType> targetMobs = new HashSet<>();
    private final Set<EntityType> killedMobs = new HashSet<>();

    public ProjectManager() {
        // Alle standardmäßig zu tötenden Mob-Typen registrieren
        targetMobs.addAll(Arrays.asList(
            EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN,
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER,
            EntityType.ENDERMAN, EntityType.WITCH, EntityType.SLIME, EntityType.VILLAGER,
            EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.BLAZE, EntityType.GHAST,
            EntityType.MAGMA_CUBE, EntityType.PIGLIN, EntityType.HOGLIN, EntityType.WITHER_SKELETON,
            EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.SQUID, EntityType.BAT,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.DROWNED,
            EntityType.HUSK, EntityType.STRAY, EntityType.CAVE_SPIDER, EntityType.SILVERFISH,
            EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.PHANTOM, EntityType.VEX,
            EntityType.VINDICATOR, EntityType.EVOKER, EntityType.PILLAGER, EntityType.RAVAGER,
            EntityType.WANDERING_TRADER, EntityType.LLAMA, EntityType.HORSE, EntityType.DONKEY,
            EntityType.MULE, EntityType.WOLF, EntityType.OCELOT, EntityType.CAT,
            EntityType.FOX, EntityType.PANDA, EntityType.TURTLE, EntityType.POLAR_BEAR,
            EntityType.DOLPHIN, EntityType.BEE, EntityType.STRIDER, EntityType.SHULKER
        ));
    }

    public void registerKill(EntityType type) {
        if (targetMobs.contains(type)) {
            killedMobs.add(type);
        }
    }

    public Set<EntityType> getTargetMobs() {
        return targetMobs;
    }

    public Set<EntityType> getKilledMobs() {
        return killedMobs;
    }

    public Set<EntityType> getRemainingMobs() {
        Set<EntityType> remaining = new HashSet<>(targetMobs);
        remaining.removeAll(killedMobs);
        return remaining;
    }

    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void reset() {
        killedMobs.clear();
    }
}
