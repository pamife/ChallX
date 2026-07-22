package me.pamife.challX.manager;

import me.pamife.challX.ChallX;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ProjectManager {

    public static class DeathTarget {
        private final String displayName;
        private final String description;
        private final DamageCause cause;
        private final EntityType mobType;

        public DeathTarget(String displayName, String description, DamageCause cause, EntityType mobType) {
            this.displayName = displayName;
            this.description = description;
            this.cause = cause;
            this.mobType = mobType;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public DamageCause getCause() { return cause; }
        public EntityType getMobType() { return mobType; }
    }

    // --- 1. Alle Mobs töten ---
    private boolean mobsEnabled = false;
    private final Set<EntityType> targetMobs = new HashSet<>();
    private final Set<EntityType> killedMobs = new HashSet<>();

    // --- 2. Alle Items sammeln ---
    private boolean itemsEnabled = false;
    private int currentItemIndex = 0;
    private static final List<Material> TARGET_ITEMS = new ArrayList<>();

    static {
        for (Material m : Material.values()) {
            if (m.isItem() && !m.isLegacy() && m != Material.AIR && m != Material.BEDROCK && m != Material.BARRIER && 
                m != Material.COMMAND_BLOCK && m != Material.CHAIN_COMMAND_BLOCK && m != Material.REPEATING_COMMAND_BLOCK && 
                m != Material.STRUCTURE_BLOCK && m != Material.STRUCTURE_VOID && m != Material.JIGSAW && m != Material.LIGHT) {
                TARGET_ITEMS.add(m);
            }
        }
    }

    // --- 3. Alle Todesnachrichten ---
    private boolean deathsEnabled = false;
    private int currentDeathIndex = 0;
    private BossBar deathBossBar;

    private static final List<DeathTarget> TARGET_DEATHS = Arrays.asList(
            new DeathTarget("Kaktus", "Stirb an einem Kaktus", DamageCause.CONTACT, null),
            new DeathTarget("Fallschaden", "Stirb durch Fallschaden", DamageCause.FALL, null),
            new DeathTarget("Ertrinken", "Ertrinke im Wasser", DamageCause.DROWNING, null),
            new DeathTarget("Ersticken", "Erstikke in einer Wand (Sand/Kies)", DamageCause.SUFFOCATION, null),
            new DeathTarget("Feuer", "Verbrenne im Feuer", DamageCause.FIRE, null),
            new DeathTarget("Lava", "Ertrinke in Lava", DamageCause.LAVA, null),
            new DeathTarget("Zombie", "Werde von einem Zombie getötet", DamageCause.ENTITY_ATTACK, EntityType.ZOMBIE),
            new DeathTarget("Skelett", "Werde von einem Skelett erschossen", DamageCause.PROJECTILE, EntityType.SKELETON),
            new DeathTarget("Creeper", "Werde von einem Creeper in die Luft gejagt", DamageCause.BLOCK_EXPLOSION, EntityType.CREEPER),
            new DeathTarget("Spinne", "Werde von einer Spinne getötet", DamageCause.ENTITY_ATTACK, EntityType.SPIDER),
            new DeathTarget("Drachenatem", "Werde vom Drachenatem geröstet", DamageCause.DRAGON_BREATH, EntityType.ENDER_DRAGON),
            new DeathTarget("Void", "Falle in die Leere (Void)", DamageCause.VOID, null),
            new DeathTarget("Magmablock", "Verbrenne auf einem Magmablock (Hot Floor)", DamageCause.HOT_FLOOR, null),
            new DeathTarget("Fallender Amboss", "Werde von einem fallenden Amboss erschlagen", DamageCause.FALLING_BLOCK, null),
            new DeathTarget("Fallender Tropfstein", "Werde von einem fallenden Tropfstein erschlagen", DamageCause.FALLING_BLOCK, null),
            new DeathTarget("Stalagmit", "Falle auf einen Tropfstein (Stalagmite)", DamageCause.FALL, null),
            new DeathTarget("Ghast-Feuerball", "Werde von einem Ghast-Feuerball getroffen", DamageCause.PROJECTILE, EntityType.GHAST),
            new DeathTarget("Feuerwerk", "Explodiere durch ein Feuerwerk", DamageCause.ENTITY_EXPLOSION, null),
            new DeathTarget("Elytra-Wand", "Krache mit Elytra gegen eine Wand (Kinetic Energy)", DamageCause.FLY_INTO_WALL, null),
            new DeathTarget("Pulverschnee", "Friere in Pulverschnee zu Tode", DamageCause.FREEZE, null),
            new DeathTarget("Blitz", "Werde vom Blitz getroffen", DamageCause.LIGHTNING, null),
            new DeathTarget("Hexe", "Werde von einer Hexe (Magie/Trank) getötet", DamageCause.MAGIC, EntityType.WITCH),
            new DeathTarget("Verhungern", "Verhungere (Hunger = 0)", DamageCause.STARVATION, null),
            new DeathTarget("Beerenbusch", "Stirb in einem Beerenbusch", DamageCause.CONTACT, null),
            new DeathTarget("Warden Sonic Boom", "Werde vom Warden-Schrei (Sonic Boom) getötet", DamageCause.SONIC_BOOM, EntityType.WARDEN),
            new DeathTarget("Ertrunkener Dreizack", "Werde von einem Ertrunkenen mit Dreizack getötet", DamageCause.PROJECTILE, EntityType.DROWNED),
            new DeathTarget("Biene", "Werde von einer Biene gestochen", DamageCause.ENTITY_ATTACK, EntityType.BEE),
            new DeathTarget("Lama-Spucke", "Werde von Lama-Spucke getötet", DamageCause.PROJECTILE, EntityType.LLAMA),
            new DeathTarget("Breeze Wind Charge", "Werde von einer Breeze getötet", DamageCause.PROJECTILE, EntityType.BREEZE),
            new DeathTarget("Wither Skelett", "Werde vom Wither-Effekt getötet", DamageCause.WITHER, EntityType.WITHER_SKELETON),
            new DeathTarget("WorldBorder", "Verlasse die WorldBorder", DamageCause.WORLD_BORDER, null),
            new DeathTarget("Schleim", "Werde von einem Schleim getötet", DamageCause.ENTITY_ATTACK, EntityType.SLIME),
            new DeathTarget("Eisengolem", "Werde von einem Eisengolem getötet", DamageCause.ENTITY_ATTACK, EntityType.IRON_GOLEM),
            new DeathTarget("Enderman", "Werde von einem Enderman getötet", DamageCause.ENTITY_ATTACK, EntityType.ENDERMAN),
            new DeathTarget("Lohe", "Werde von einer Lohe getötet", DamageCause.ENTITY_ATTACK, EntityType.BLAZE),
            new DeathTarget("Piglin Brute", "Werde von einem Piglin Brute getötet", DamageCause.ENTITY_ATTACK, EntityType.PIGLIN_BRUTE),
            new DeathTarget("Phantom", "Werde von einem Phantom getötet", DamageCause.ENTITY_ATTACK, EntityType.PHANTOM),
            new DeathTarget("Wächter", "Werde von einem Wächter getötet", DamageCause.MAGIC, EntityType.GUARDIAN),
            new DeathTarget("Silberfischchen", "Werde von einem Silberfischchen getötet", DamageCause.ENTITY_ATTACK, EntityType.SILVERFISH),
            new DeathTarget("TNT Explosion", "Werde durch TNT-Explosion getötet", DamageCause.BLOCK_EXPLOSION, null)
    );

    // --- 4. Alle Achievements ---
    private boolean achievementsEnabled = false;
    private final Set<String> completedAchievements = new HashSet<>();

    private BukkitTask projectTask;

    public ProjectManager() {
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

        startProjectTask();
    }

    private void startProjectTask() {
        projectTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                // 1. Items sammeln check
                if (itemsEnabled && currentItemIndex < TARGET_ITEMS.size()) {
                    Material target = TARGET_ITEMS.get(currentItemIndex);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
                        if (p.getInventory().contains(target)) {
                            advanceItem(p);
                            break;
                        }
                    }

                    Component msg = Component.text("§a[Projekt] Nächstes Item: §e" + target.name() + " §7(" + (currentItemIndex + 1) + "/" + TARGET_ITEMS.size() + ")");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendActionBar(msg);
                    }
                }

                // 2. Bossbar für Todesnachrichten Projekt aktualisieren
                if (deathsEnabled && currentDeathIndex < TARGET_DEATHS.size()) {
                    updateDeathBossBar();
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L);
    }

    // --- MOB KILL LOGIC ---
    public void registerKill(EntityType type) {
        if (targetMobs.contains(type)) {
            killedMobs.add(type);
        }
    }

    public Set<EntityType> getTargetMobs() { return targetMobs; }
    public Set<EntityType> getKilledMobs() { return killedMobs; }
    public Set<EntityType> getRemainingMobs() {
        Set<EntityType> remaining = new HashSet<>(targetMobs);
        remaining.removeAll(killedMobs);
        return remaining;
    }

    public boolean isMobsEnabled() { return mobsEnabled; }
    public void setMobsEnabled(boolean mobsEnabled) { this.mobsEnabled = mobsEnabled; }

    // --- ITEM COLLECT LOGIC ---
    public boolean isItemsEnabled() { return itemsEnabled; }
    public void setItemsEnabled(boolean itemsEnabled) { this.itemsEnabled = itemsEnabled; }
    public int getCurrentItemIndex() { return currentItemIndex; }
    public void setCurrentItemIndex(int idx) { this.currentItemIndex = idx; }
    public List<Material> getTargetItems() { return TARGET_ITEMS; }

    public void advanceItem(Player finder) {
        Material target = TARGET_ITEMS.get(currentItemIndex);
        Bukkit.broadcastMessage("§a[Projekt] §2" + finder.getName() + " §7hat §e" + target.name() + " §7gesammelt!");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
        currentItemIndex++;

        if (currentItemIndex >= TARGET_ITEMS.size()) {
            Bukkit.broadcastMessage("§a[Projekt] §2§lHerzlichen Glückwunsch! Alle " + TARGET_ITEMS.size() + " Items wurden gesammelt!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("§a§lProjekt abgeschlossen!", "§eAlle Items gesammelt.", 10, 70, 20);
            }
            itemsEnabled = false;
        }
    }

    public void skipItem(Player skipper) {
        if (!itemsEnabled) return;
        Material old = TARGET_ITEMS.get(currentItemIndex);
        Bukkit.broadcastMessage("§e[Projekt] §6" + skipper.getName() + " §7hat das Item §e" + old.name() + " §7übersprungen.");
        currentItemIndex++;

        if (currentItemIndex >= TARGET_ITEMS.size()) {
            Bukkit.broadcastMessage("§a[Projekt] §2§lHerzlichen Glückwunsch! Alle Items wurden gesammelt!");
            itemsEnabled = false;
        }
    }

    // --- DEATH CAUSE LOGIC ---
    public boolean isDeathsEnabled() { return deathsEnabled; }
    
    public void setDeathsEnabled(boolean deathsEnabled) {
        this.deathsEnabled = deathsEnabled;
        if (deathsEnabled) {
            updateDeathBossBar();
        } else if (deathBossBar != null) {
            deathBossBar.removeAll();
            deathBossBar = null;
        }
    }

    public int getCurrentDeathIndex() { return currentDeathIndex; }
    public void setCurrentDeathIndex(int idx) { this.currentDeathIndex = idx; }
    public List<DeathTarget> getTargetDeaths() { return TARGET_DEATHS; }

    private void updateDeathBossBar() {
        if (currentDeathIndex >= TARGET_DEATHS.size()) {
            if (deathBossBar != null) {
                deathBossBar.removeAll();
                deathBossBar = null;
            }
            return;
        }

        DeathTarget target = TARGET_DEATHS.get(currentDeathIndex);
        String title = "§c[Projekt Tod #" + (currentDeathIndex + 1) + "/" + TARGET_DEATHS.size() + "] §e" + target.getDescription();

        if (deathBossBar == null) {
            deathBossBar = Bukkit.createBossBar(title, BarColor.RED, BarStyle.SOLID);
        } else {
            deathBossBar.setTitle(title);
        }

        deathBossBar.setProgress((double) currentDeathIndex / (double) TARGET_DEATHS.size());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) {
                if (!deathBossBar.getPlayers().contains(p)) {
                    deathBossBar.addPlayer(p);
                }
            } else {
                deathBossBar.removePlayer(p);
            }
        }
    }

    public void registerDeath(Player player, EntityDamageEvent damageEvent) {
        if (!deathsEnabled) return;
        if (currentDeathIndex >= TARGET_DEATHS.size()) return;

        DeathTarget target = TARGET_DEATHS.get(currentDeathIndex);
        DamageCause cause = damageEvent.getCause();

        boolean causeMatch = (cause == target.getCause());
        boolean mobMatch = true;

        if (target.getMobType() != null) {
            mobMatch = false;
            if (damageEvent instanceof EntityDamageByEntityEvent byEntity) {
                Entity damager = byEntity.getDamager();
                if (damager.getType() == target.getMobType()) {
                    mobMatch = true;
                } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity shooter) {
                    if (shooter.getType() == target.getMobType()) {
                        mobMatch = true;
                    }
                }
            }
        }

        if (causeMatch && mobMatch) {
            Bukkit.broadcastMessage("§a[Projekt Todesnachrichten] §2" + player.getName() + " §7hat Tod #" + (currentDeathIndex + 1) + " (" + target.getDisplayName() + ") erfolgreich absolviert!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            }
            currentDeathIndex++;

            if (currentDeathIndex >= TARGET_DEATHS.size()) {
                Bukkit.broadcastMessage("§a[Projekt] §2§lHERZLICHEN GLÜCKWUNSCH! Alle 40 Todesnachrichten wurden gemeistert!");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle("§a§lProjekt abgeschlossen!", "§eAlle Todesnachrichten gemeistert.", 10, 80, 20);
                }
                setDeathsEnabled(false);
            } else {
                updateDeathBossBar();
            }
        } else {
            ChallX.getInstance().getTimerManager().pause();
            Bukkit.broadcastMessage("§c[Projekt Todesnachrichten] §4FALSCHER TOD! §e" + player.getName() + " ist an §6" + cause.name() + " §egestorben statt an §2" + target.getDisplayName() + "§e!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("§c§lProjekt Fehlgeschlagen!", "§eFalsche Todesursache.", 10, 70, 20);
            }
            currentDeathIndex = 0;
            setDeathsEnabled(false);
        }
    }

    // --- ACHIEVEMENTS LOGIC ---
    public boolean isAchievementsEnabled() { return achievementsEnabled; }
    public void setAchievementsEnabled(boolean achievementsEnabled) { this.achievementsEnabled = achievementsEnabled; }
    public Set<String> getCompletedAchievements() { return completedAchievements; }

    public void registerAchievement(Player player, String advKey) {
        if (!achievementsEnabled) return;
        if (completedAchievements.contains(advKey)) return;

        completedAchievements.add(advKey);
        int total = getMinecraftAdvancementCount();
        
        Bukkit.broadcastMessage("§a[Projekt] §2" + player.getName() + " §7hat das Advancement §e" + advKey + " §7freigeschaltet! (" + completedAchievements.size() + "/" + total + ")");

        if (completedAchievements.size() >= total) {
            Bukkit.broadcastMessage("§a[Projekt] §2§lHerzlichen Glückwunsch! Alle Achievements wurden freigeschaltet!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("§a§lProjekt abgeschlossen!", "§eAlle Achievements freigeschaltet.", 10, 70, 20);
            }
            achievementsEnabled = false;
        }
    }

    public int getMinecraftAdvancementCount() {
        int count = 0;
        var iter = Bukkit.advancementIterator();
        while (iter.hasNext()) {
            var adv = iter.next();
            if (!adv.getKey().getKey().startsWith("recipes/")) {
                count++;
            }
        }
        return count;
    }

    // --- ZENTRALE STEUERUNG ---
    @Deprecated
    public boolean isEnabled() { return mobsEnabled; }
    @Deprecated
    public void setEnabled(boolean state) { this.mobsEnabled = state; }

    public void reset() {
        killedMobs.clear();
        currentItemIndex = 0;
        currentDeathIndex = 0;
        completedAchievements.clear();
        
        mobsEnabled = false;
        itemsEnabled = false;
        setDeathsEnabled(false);
        achievementsEnabled = false;
    }
}
