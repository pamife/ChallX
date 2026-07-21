package me.pamife.challX.manager;

import me.pamife.challX.ChallX;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ProjectManager {

    // --- 1. Alle Mobs töten ---
    private boolean mobsEnabled = false;
    private final Set<EntityType> targetMobs = new HashSet<>();
    private final Set<EntityType> killedMobs = new HashSet<>();

    // --- 2. Alle Items sammeln ---
    private boolean itemsEnabled = false;
    private int currentItemIndex = 0;
    private static final List<Material> TARGET_ITEMS = Arrays.asList(
            Material.DIRT, Material.COBBLESTONE, Material.OAK_LOG, Material.COAL, Material.IRON_INGOT,
            Material.GOLD_INGOT, Material.REDSTONE, Material.LAPIS_LAZULI, Material.DIAMOND, Material.OBSIDIAN,
            Material.NETHERRACK, Material.SOUL_SAND, Material.GLOWSTONE_DUST, Material.BLAZE_ROD, Material.ENDER_PEARL,
            Material.NETHERITE_SCRAP, Material.NETHERITE_INGOT, Material.APPLE, Material.BREAD, Material.COOKED_BEEF,
            Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.MILK_BUCKET, Material.SHIELD, Material.BOW,
            Material.CROSSBOW, Material.TRIDENT, Material.ELYTRA, Material.TOTEM_OF_UNDYING, Material.SHULKER_BOX
    );

    // --- 3. Alle Todesnachrichten ---
    private boolean deathsEnabled = false;
    private int currentDeathIndex = 0;
    private static final List<DamageCause> TARGET_DEATHS = Arrays.asList(
            DamageCause.CONTACT,         // Kaktus
            DamageCause.FALL,            // Fallschaden
            DamageCause.DROWNING,        // Ertrinken
            DamageCause.SUFFOCATION,     // Ersticken (in der Wand)
            DamageCause.FIRE,            // Feuer
            DamageCause.LAVA,            // Lava
            DamageCause.ENTITY_ATTACK,   // Mob-Angriff
            DamageCause.MAGIC,           // Hexe / Potion
            DamageCause.STARVATION,      // Verhungern
            DamageCause.VOID             // Void (Ende der Welt)
    );

    // --- 4. Alle Achievements ---
    private boolean achievementsEnabled = false;
    private final Set<String> completedAchievements = new HashSet<>();

    private BukkitTask projectTask;

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

        startProjectTask();
    }

    private void startProjectTask() {
        projectTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) return;

                // 1. Alle Items sammeln check
                if (itemsEnabled && currentItemIndex < TARGET_ITEMS.size()) {
                    Material target = TARGET_ITEMS.get(currentItemIndex);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (ChallX.getInstance().getSettingsManager().isExcluded(p.getUniqueId())) continue;
                        if (p.getInventory().contains(target)) {
                            advanceItem(p);
                            break;
                        }
                    }

                    // ActionBar-Visualisierung
                    Component msg = Component.text("§a[Projekt] Nächstes Item: §e" + target.name() + " §7(" + (currentItemIndex + 1) + "/" + TARGET_ITEMS.size() + ")");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendActionBar(msg);
                    }
                }

                // 2. Alle Todesnachrichten Actionbar
                if (deathsEnabled && currentDeathIndex < TARGET_DEATHS.size()) {
                    DamageCause target = TARGET_DEATHS.get(currentDeathIndex);
                    Component msg = Component.text("§c[Projekt] Nächster Tod: §e" + getDeathCauseName(target) + " §7(" + (currentDeathIndex + 1) + "/" + TARGET_DEATHS.size() + ")");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendActionBar(msg);
                    }
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
            Bukkit.broadcastMessage("§a[Projekt] §2§lHerzlichen Glückwunsch! Alle Items wurden gesammelt!");
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
    public void setDeathsEnabled(boolean deathsEnabled) { this.deathsEnabled = deathsEnabled; }
    public int getCurrentDeathIndex() { return currentDeathIndex; }
    public void setCurrentDeathIndex(int idx) { this.currentDeathIndex = idx; }
    public List<DamageCause> getTargetDeaths() { return TARGET_DEATHS; }

    public void registerDeath(Player player, DamageCause cause) {
        if (!deathsEnabled) return;
        
        DamageCause target = TARGET_DEATHS.get(currentDeathIndex);
        if (cause == target) {
            Bukkit.broadcastMessage("§a[Projekt] §2" + player.getName() + " §7ist gestorben durch §e" + getDeathCauseName(cause) + "§7! Richtig!");
            currentDeathIndex++;

            if (currentDeathIndex >= TARGET_DEATHS.size()) {
                Bukkit.broadcastMessage("§a[Projekt] §2§lHerzlichen Glückwunsch! Alle Todesnachrichten wurden absolviert!");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle("§a§lProjekt abgeschlossen!", "§eAlle Todesnachrichten absolviert.", 10, 70, 20);
                }
                deathsEnabled = false;
            }
        } else {
            // Falscher Tod = Fehlgeschlagen!
            ChallX.getInstance().getTimerManager().pause();
            Bukkit.broadcastMessage("§c[Projekt] §4Falscher Tod! §e" + player.getName() + " ist an §6" + getDeathCauseName(cause) + " §egestorben statt an §2" + getDeathCauseName(target) + "§e!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("§c§lProjekt Fehlgeschlagen!", "§eFalsche Todesart erlitten.", 10, 70, 20);
            }
            // Zurücksetzen
            currentDeathIndex = 0;
            deathsEnabled = false;
        }
    }

    public String getDeathCauseName(DamageCause cause) {
        return switch (cause) {
            case CONTACT -> "Kaktus / Beeren";
            case FALL -> "Fallschaden";
            case DROWNING -> "Ertrinken";
            case SUFFOCATION -> "Ersticken in Wand";
            case FIRE -> "Feuer";
            case LAVA -> "Lava";
            case ENTITY_ATTACK -> "Mob-Angriff";
            case MAGIC -> "Magie (Trank/Hexe)";
            case STARVATION -> "Verhungern";
            case VOID -> "Die Leere (Void)";
            default -> cause.name();
        };
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
        deathsEnabled = false;
        achievementsEnabled = false;
    }
}
