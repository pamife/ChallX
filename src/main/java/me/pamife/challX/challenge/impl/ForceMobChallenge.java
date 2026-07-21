package me.pamife.challX.challenge.impl;

import me.pamife.challX.ChallX;
import me.pamife.challX.challenge.BaseChallenge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ForceMobChallenge extends BaseChallenge {

    public static class ForceMobTarget {
        public final String displayName;
        public final EntityType mainType;
        public final EntityType vehicleType; // null falls kein Reittier
        public final boolean baby; // true falls es ein Baby sein muss

        public ForceMobTarget(String displayName, EntityType mainType) {
            this(displayName, mainType, null, false);
        }

        public ForceMobTarget(String displayName, EntityType mainType, EntityType vehicleType, boolean baby) {
            this.displayName = displayName;
            this.mainType = mainType;
            this.vehicleType = vehicleType;
            this.baby = baby;
        }
    }

    private static final List<ForceMobTarget> TARGETS = Arrays.asList(
            new ForceMobTarget("Allay", EntityType.ALLAY),
            new ForceMobTarget("Gürteltier (Armadillo)", EntityType.ARMADILLO),
            new ForceMobTarget("Axolotl", EntityType.AXOLOTL),
            new ForceMobTarget("Fledermaus", EntityType.BAT),
            new ForceMobTarget("Kamel", EntityType.CAMEL),
            new ForceMobTarget("Katze", EntityType.CAT),
            new ForceMobTarget("Huhn", EntityType.CHICKEN),
            new ForceMobTarget("Kabeljau", EntityType.COD),
            new ForceMobTarget("Kuh", EntityType.COW),
            new ForceMobTarget("Esel", EntityType.DONKEY),
            new ForceMobTarget("Frosch", EntityType.FROG),
            new ForceMobTarget("Leucht-Tintenfisch", EntityType.GLOW_SQUID),
            new ForceMobTarget("Pferd", EntityType.HORSE),
            new ForceMobTarget("Pilzkuh (Mooshroom)", EntityType.MOOSHROOM),
            new ForceMobTarget("Maultier", EntityType.MULE),
            new ForceMobTarget("Ozelot", EntityType.OCELOT),
            new ForceMobTarget("Papagei", EntityType.PARROT),
            new ForceMobTarget("Schwein", EntityType.PIG),
            new ForceMobTarget("Kaninchen", EntityType.RABBIT),
            new ForceMobTarget("Lachs", EntityType.SALMON),
            new ForceMobTarget("Schaf", EntityType.SHEEP),
            new ForceMobTarget("Skelett-Pferd", EntityType.SKELETON_HORSE),
            new ForceMobTarget("Schnüffler (Sniffer)", EntityType.SNIFFER),
            new ForceMobTarget("Schneegolem", EntityType.SNOW_GOLEM),
            new ForceMobTarget("Tintenfisch", EntityType.SQUID),
            new ForceMobTarget("Schreiter (Strider)", EntityType.STRIDER),
            new ForceMobTarget("Kaulquappe", EntityType.TADPOLE),
            new ForceMobTarget("Tropenfisch", EntityType.TROPICAL_FISH),
            new ForceMobTarget("Schildkröte", EntityType.TURTLE),
            new ForceMobTarget("Dorfbewohner (Villager)", EntityType.VILLAGER),
            new ForceMobTarget("Fahrender Händler", EntityType.WANDERING_TRADER),
            new ForceMobTarget("Zombie-Pferd", EntityType.ZOMBIE_HORSE),
            new ForceMobTarget("Biene", EntityType.BEE),
            new ForceMobTarget("Höhlenspinne", EntityType.CAVE_SPIDER),
            new ForceMobTarget("Delphin", EntityType.DOLPHIN),
            new ForceMobTarget("Ertrunkener (Drowned)", EntityType.DROWNED),
            new ForceMobTarget("Enderman", EntityType.ENDERMAN),
            new ForceMobTarget("Fuchs", EntityType.FOX),
            new ForceMobTarget("Ziege", EntityType.GOAT),
            new ForceMobTarget("Eisengolem", EntityType.IRON_GOLEM),
            new ForceMobTarget("Lama", EntityType.LLAMA),
            new ForceMobTarget("Panda", EntityType.PANDA),
            new ForceMobTarget("Piglin", EntityType.PIGLIN),
            new ForceMobTarget("Eisbär", EntityType.POLAR_BEAR),
            new ForceMobTarget("Kugelfisch", EntityType.PUFFERFISH),
            new ForceMobTarget("Spinne", EntityType.SPIDER),
            new ForceMobTarget("Händlerlama", EntityType.TRADER_LLAMA),
            new ForceMobTarget("Wolf", EntityType.WOLF),
            new ForceMobTarget("Zombifizierter Piglin", EntityType.ZOMBIFIED_PIGLIN),
            new ForceMobTarget("Lohe (Blaze)", EntityType.BLAZE),
            new ForceMobTarget("Moor-Skelett (Bogged)", EntityType.BOGGED),
            new ForceMobTarget("Breeze", EntityType.BREEZE),
            new ForceMobTarget("Creaker (Creaking)", EntityType.CREAKING),
            new ForceMobTarget("Creeper", EntityType.CREEPER),
            new ForceMobTarget("Großer Wächter", EntityType.ELDER_GUARDIAN),
            new ForceMobTarget("Enderdrache", EntityType.ENDER_DRAGON),
            new ForceMobTarget("Endermite", EntityType.ENDERMITE),
            new ForceMobTarget("Magier (Evoker)", EntityType.EVOKER),
            new ForceMobTarget("Ghast", EntityType.GHAST),
            new ForceMobTarget("Wächter (Guardian)", EntityType.GUARDIAN),
            new ForceMobTarget("Hoglin", EntityType.HOGLIN),
            new ForceMobTarget("Wüstenzombie (Husk)", EntityType.HUSK),
            new ForceMobTarget("Magmaschleim", EntityType.MAGMA_CUBE),
            new ForceMobTarget("Phantom", EntityType.PHANTOM),
            new ForceMobTarget("Piglin-Brutale", EntityType.PIGLIN_BRUTE),
            new ForceMobTarget("Plünderer (Pillager)", EntityType.PILLAGER),
            new ForceMobTarget("Verwüster (Ravager)", EntityType.RAVAGER),
            new ForceMobTarget("Shulker", EntityType.SHULKER),
            new ForceMobTarget("Silberfischchen", EntityType.SILVERFISH),
            new ForceMobTarget("Skelett", EntityType.SKELETON),
            new ForceMobTarget("Schleim (Slime)", EntityType.SLIME),
            new ForceMobTarget("Eiswanderer (Stray)", EntityType.STRAY),
            new ForceMobTarget("Vex", EntityType.VEX),
            new ForceMobTarget("Diener (Vindicator)", EntityType.VINDICATOR),
            new ForceMobTarget("Warden", EntityType.WARDEN),
            new ForceMobTarget("Hexe (Witch)", EntityType.WITCH),
            new ForceMobTarget("Wither", EntityType.WITHER),
            new ForceMobTarget("Wither-Skelett", EntityType.WITHER_SKELETON),
            new ForceMobTarget("Zoglin", EntityType.ZOGLIN),
            new ForceMobTarget("Zombie", EntityType.ZOMBIE),
            new ForceMobTarget("Zombie-Dorfbewohner", EntityType.ZOMBIE_VILLAGER),

            // Jockeys
            new ForceMobTarget("Chicken Jockey", EntityType.ZOMBIE, EntityType.CHICKEN, true),
            new ForceMobTarget("Spider Jockey", EntityType.SKELETON, EntityType.SPIDER, false),
            new ForceMobTarget("Skeleton Horseman", EntityType.SKELETON, EntityType.SKELETON_HORSE, false),
            new ForceMobTarget("Ravager Jockey", EntityType.PILLAGER, EntityType.RAVAGER, false),
            new ForceMobTarget("Strider Jockey", EntityType.ZOMBIFIED_PIGLIN, EntityType.STRIDER, false),
            new ForceMobTarget("Hoglin Jockey", EntityType.PIGLIN, EntityType.HOGLIN, true),
            new ForceMobTarget("Zombie Horseman", EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE, false),
            new ForceMobTarget("Camel Husk Jockey", EntityType.HUSK, EntityType.CAMEL, false)
    );

    private ForceMobTarget currentTarget;
    private int timeLeft = 300; // 5 Minuten Standard
    private BossBar bossBar;
    private BukkitTask task;

    @Override
    public String getName() {
        return "Force-Mob";
    }

    @Override
    public String getDescription() {
        return "Die Spieler müssen regelmäßig vorgegebene Mobs in einer bestimmten Zeit töten.";
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = new ItemStack(Material.CREEPER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cForce-Mob");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onEnable() {
        if (currentTarget == null) {
            selectRandomTarget();
        }

        bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ChallX.getInstance().getTimerManager().isRunning()) {
                    updateBossBarText(true);
                    return;
                }

                if (timeLeft > 0) {
                    timeLeft--;
                    updateBossBarText(false);
                } else {
                    // Zeit abgelaufen! Alle Spieler töten
                    Bukkit.broadcastMessage("§cDie Zeit für das Mob §6" + currentTarget.displayName + " §cist abgelaufen!");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (ChallX.getInstance().getSettingsManager().isExcluded(player.getUniqueId())) continue;
                        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;
                        player.setHealth(0.0);
                    }
                    // Neues Mob wählen
                    selectRandomTarget();
                }
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L);
    }

    @Override
    public void onDisable() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void selectRandomTarget() {
        currentTarget = TARGETS.get(new Random().nextInt(TARGETS.size()));
        timeLeft = 300;
        updateBossBarText(false);
    }

    private void updateBossBarText(boolean paused) {
        if (bossBar == null || currentTarget == null) return;

        double progress = Math.max(0.0, Math.min(1.0, (double) timeLeft / 300.0));
        bossBar.setProgress(progress);

        if (progress > 0.5) {
            bossBar.setColor(BarColor.GREEN);
        } else if (progress > 0.2) {
            bossBar.setColor(BarColor.YELLOW);
        } else {
            bossBar.setColor(BarColor.RED);
        }

        String timeStr = formatTime(timeLeft);
        String title = "§eTöte: §6§l" + currentTarget.displayName + " §7| §eZeit: §c" + timeStr;
        if (paused) {
            title += " §7(Pausiert)";
        }
        bossBar.setTitle(title);
    }

    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (isEnabled() && bossBar != null) {
            bossBar.addPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (!isEnabled()) return;
        if (!ChallX.getInstance().getTimerManager().isRunning()) return;

        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;
        if (ChallX.getInstance().getSettingsManager().isExcluded(killer.getUniqueId())) return;

        if (currentTarget == null) return;

        // Prüfen ob Typ übereinstimmt
        if (entity.getType() == currentTarget.mainType) {
            // Reittier prüfen
            if (currentTarget.vehicleType != null) {
                Entity vehicle = entity.getVehicle();
                if (vehicle == null || vehicle.getType() != currentTarget.vehicleType) {
                    return;
                }
            }

            // Baby-Status prüfen
            if (currentTarget.baby) {
                if (entity instanceof Ageable ageable && !ageable.isAdult() == false) {
                    return; // Ist erwachsen, aber Baby wird gesucht
                }
                if (entity instanceof Zombie zombie && !zombie.isBaby()) {
                    return;
                }
                if (entity instanceof Piglin piglin && !piglin.isBaby()) {
                    return;
                }
            }

            // Kill erfolgreich!
            Bukkit.broadcastMessage("§a[Force-Mob] §2" + killer.getName() + " §7hat das gesuchte Mob (§e" + currentTarget.displayName + "§7) getötet!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("§a§lErfolgreich!", "§e" + currentTarget.displayName + " wurde getötet.", 10, 50, 10);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }

            selectRandomTarget();
        }
    }

    @Override
    public Object getSettingsState() {
        Map<String, Object> state = new HashMap<>();
        state.put("target", currentTarget != null ? currentTarget.displayName : null);
        state.put("timeLeft", timeLeft);
        return state;
    }

    @Override
    public void loadSettingsState(Object state) {
        if (state instanceof Map<?, ?> map) {
            Object targetVal = map.get("target");
            if (targetVal instanceof String name) {
                for (ForceMobTarget target : TARGETS) {
                    if (target.displayName.equalsIgnoreCase(name)) {
                        this.currentTarget = target;
                        break;
                    }
                }
            }
            Object timeVal = map.get("timeLeft");
            if (timeVal instanceof Number num) {
                this.timeLeft = num.intValue();
            }
        }
    }
}
