package me.pamife.challX.listener;

import me.pamife.challX.ChallX;
import me.pamife.challX.manager.ProjectManager;
import me.pamife.challX.manager.SettingsManager;
import me.pamife.challX.setting.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.GameMode;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SettingsAndProjectListener implements Listener {

    private final Map<Material, Material> cutCleanOres = new HashMap<>();
    private final Map<Material, Material> cutCleanMeat = new HashMap<>();

    public SettingsAndProjectListener() {
        // Cut Clean Erz-Zuweisung
        cutCleanOres.put(Material.RAW_IRON, Material.IRON_INGOT);
        cutCleanOres.put(Material.IRON_ORE, Material.IRON_INGOT);
        cutCleanOres.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        cutCleanOres.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        cutCleanOres.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        cutCleanOres.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        cutCleanOres.put(Material.RAW_COPPER, Material.COPPER_INGOT);
        cutCleanOres.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        cutCleanOres.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);

        // Cut Clean Fleisch-Zuweisung
        cutCleanMeat.put(Material.BEEF, Material.COOKED_BEEF);
        cutCleanMeat.put(Material.PORKCHOP, Material.COOKED_PORKCHOP);
        cutCleanMeat.put(Material.CHICKEN, Material.COOKED_CHICKEN);
        cutCleanMeat.put(Material.MUTTON, Material.COOKED_MUTTON);
        cutCleanMeat.put(Material.RABBIT, Material.COOKED_RABBIT);
    }

    // --- Player Join: Sync Max Health ---
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isExcluded(player)) return;

        double maxHealth = getSM().getIntSetting(Setting.MAX_HEALTH) * 2.0;
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
    }

    private SettingsManager getSM() {
        return ChallX.getInstance().getSettingsManager();
    }

    private boolean isExcluded(Player player) {
        return getSM().isExcluded(player.getUniqueId());
    }

    // --- PVP Toggle ---
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPVP(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if (!getSM().getSetting(Setting.PVP)) {
                event.setCancelled(true);
            }
        }
    }

    // --- Natural Regeneration ---
    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            if (!getSM().getSetting(Setting.NATURAL_REGEN)) {
                if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED || 
                    event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // --- Schaden im Chat & Geteilte Herzen & Ein Leben für alle ---
    private boolean syncRunning = false; // Verhindert Endlosschleifen bei Geteilte Herzen

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (isExcluded(player)) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        double damage = event.getFinalDamage();

        // 1. Schaden im Chat anzeigen
        if (getSM().getSetting(Setting.DAMAGE_IN_CHAT)) {
            Bukkit.broadcastMessage("§c[Schaden] §e" + player.getName() + " §7hat §c" + String.format("%.1f", damage) + " HP §7Schaden erlitten. (Grund: " + event.getCause().name() + ")");
        }

        // 2. Geteilte Herzen synchronisieren
        if (getSM().getSetting(Setting.SHARED_HEARTS) && !syncRunning && damage > 0) {
            syncRunning = true;
            double newHealth = Math.max(0.0, player.getHealth() - damage);
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (isExcluded(p) || p.getUniqueId().equals(player.getUniqueId())) continue;
                
                // Max Health holen
                double maxHealth = p.getAttribute(Attribute.MAX_HEALTH).getValue();
                p.setHealth(Math.min(maxHealth, newHealth));
            }
            syncRunning = false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (isExcluded(player)) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        // Geteilte Herzen Heilung synchronisieren
        if (getSM().getSetting(Setting.SHARED_HEARTS) && !syncRunning) {
            syncRunning = true;
            double healAmount = event.getAmount();
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (isExcluded(p) || p.getUniqueId().equals(player.getUniqueId())) continue;
                
                double maxHealth = p.getAttribute(Attribute.MAX_HEALTH).getValue();
                double newHealth = Math.min(maxHealth, p.getHealth() + healAmount);
                p.setHealth(newHealth);
            }
            syncRunning = false;
        }
    }

    // --- Ein Leben für alle ---
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (isExcluded(player)) return;

        if (getSM().getSetting(Setting.ONE_LIFE_FOR_ALL)) {
            ChallX.getInstance().getTimerManager().pause();
            
            // Broadcast Titel
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("§c§lChallenge vorbei!", "§e" + player.getName() + " ist gestorben.", 10, 70, 20);
            }
            Bukkit.broadcastMessage("§c§l[Challenge] Ein Leben für alle ist aktiv. Die Challenge wurde pausiert!");
        }

        // Wenn Respawn ausgeschaltet ist
        if (!getSM().getSetting(Setting.RESPAWN)) {
            Bukkit.getScheduler().runTaskLater(ChallX.getInstance(), () -> {
                player.spigot().respawn();
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage("§cDu bist gestorben und kannst nicht respawnen. Du bist nun Zuschauer!");
            }, 1L);
        }
    }

    // --- Cut Clean (Erze) ---
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!getSM().getSetting(Setting.CUT_CLEAN)) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        Block block = event.getBlock();
        Material originalMaterial = block.getType();
        
        if (cutCleanOres.containsKey(originalMaterial)) {
            Material smelted = cutCleanOres.get(originalMaterial);
            event.setDropItems(false); // Normalen Drop ausschalten
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(smelted));
        }
    }

    // --- Cut Clean (Fleisch) & Projekt: Alle Mobs töten ---
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // 1. Cut Clean (Fleisch braten)
        if (getSM().getSetting(Setting.CUT_CLEAN) && ChallX.getInstance().getTimerManager().isRunning()) {
            for (ItemStack drop : event.getDrops()) {
                if (cutCleanMeat.containsKey(drop.getType())) {
                    drop.setType(cutCleanMeat.get(drop.getType()));
                }
            }
        }

        // 2. Projekt: Alle Mobs töten
        ProjectManager pm = ChallX.getInstance().getProjectManager();
        if (entity.getKiller() != null && pm.isEnabled()) {
            Player killer = entity.getKiller();
            
            if (pm.getTargetMobs().contains(event.getEntityType())) {
                if (!pm.getKilledMobs().contains(event.getEntityType())) {
                    pm.registerKill(event.getEntityType());
                    
                    Bukkit.broadcastMessage("§a[Projekt] §2" + killer.getName() + " §7hat ein(e) §e" + event.getEntityType().name() + " §7getötet! (" + pm.getKilledMobs().size() + "/" + pm.getTargetMobs().size() + ")");
                    
                    // Prüfen ob Projekt beendet ist
                    if (pm.getRemainingMobs().isEmpty()) {
                        Bukkit.broadcastMessage("§a[Projekt] §2§lHerzlichen Glückwunsch! Alle Mobs wurden getötet!");
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendTitle("§a§lProjekt abgeschlossen!", "§eAlle Mobs wurden getötet.", 10, 70, 20);
                        }
                    }
                }
            }
        }
    }
}
