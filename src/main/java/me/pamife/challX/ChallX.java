package me.pamife.challX;

import me.pamife.challX.challenge.Challenge;
import me.pamife.challX.challenge.impl.*;
import me.pamife.challX.gui.CustomGUI;
import me.pamife.challX.gui.GUIButton;
import me.pamife.challX.gui.GUIListener;
import me.pamife.challX.listener.FreezeListener;
import me.pamife.challX.listener.SettingsAndProjectListener;
import me.pamife.challX.manager.*;
import me.pamife.challX.setting.Setting;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class ChallX extends JavaPlugin {

    private static ChallX instance;

    private ChallengeManager challengeManager;
    private ThemeManager themeManager;
    private TimerManager timerManager;
    private SettingsManager settingsManager;
    private WorldResetManager worldResetManager;
    private ProjectManager projectManager;
    private PositionManager positionManager;
    private CacheManager cacheManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Manager initialisieren
        this.themeManager = new ThemeManager();
        this.challengeManager = new ChallengeManager();
        this.timerManager = new TimerManager();
        this.settingsManager = new SettingsManager();
        this.worldResetManager = new WorldResetManager();
        this.projectManager = new ProjectManager();
        this.positionManager = new PositionManager();

        // 2. Challenges registrieren
        challengeManager.registerChallenge(new FlyOnDamageChallenge());
        challengeManager.registerChallenge(new OnlyDirtChallenge());
        challengeManager.registerChallenge(new MirrorDamageChallenge());
        challengeManager.registerChallenge(new DamageClearsInventoryChallenge());
        challengeManager.registerChallenge(new FloorIsLavaChallenge());
        challengeManager.registerChallenge(new IceFloorChallenge());
        challengeManager.registerChallenge(new ForceMobChallenge());
        challengeManager.registerChallenge(new NoCraftingChallenge());
        challengeManager.registerChallenge(new NoTradingChallenge());
        challengeManager.registerChallenge(new WalkingDamageChallenge());
        challengeManager.registerChallenge(new TNTRunChallenge());
        challengeManager.registerChallenge(new NoXPChallenge());
        challengeManager.registerChallenge(new AnvilRainChallenge());
        challengeManager.registerChallenge(new WaterMLGChallenge());
        challengeManager.registerChallenge(new TrafficLightChallenge());
        challengeManager.registerChallenge(new NoDuplicateItemsChallenge());
        challengeManager.registerChallenge(new SocialDistancingChallenge());
        challengeManager.registerChallenge(new AdvancementDamageChallenge());
        challengeManager.registerChallenge(new RandomMLGChallenge());
        challengeManager.registerChallenge(new RandomScaleChallenge());
        challengeManager.registerChallenge(new JumpDamageChallenge());
        challengeManager.registerChallenge(new SneakDamageChallenge());
        challengeManager.registerChallenge(new BlockBreakSpawnMobChallenge());
        challengeManager.registerChallenge(new RandomEffectOnDamageChallenge());
        challengeManager.registerChallenge(new NoLookingDownChallenge());
        challengeManager.registerChallenge(new HealingDamageChallenge());
        challengeManager.registerChallenge(new BlockDropRandomizerChallenge());
        challengeManager.registerChallenge(new NoInventoryOpenChallenge());
        challengeManager.registerChallenge(new ChaosPlacementChallenge());
        challengeManager.registerChallenge(new BedrockWallChallenge());
        challengeManager.registerChallenge(new BlockKillerChallenge());
        challengeManager.registerChallenge(new AlwaysRunChallenge());
        challengeManager.registerChallenge(new OneDurabilityChallenge());
        challengeManager.registerChallenge(new ChunkDecayChallenge());
        challengeManager.registerChallenge(new ForceBlockChallenge());
        challengeManager.registerChallenge(new ForceHeightChallenge());
        challengeManager.registerChallenge(new ReverseActionsChallenge());
        challengeManager.registerChallenge(new ItemDecayChallenge());
        challengeManager.registerChallenge(new NeverFullHeartsChallenge());
        challengeManager.registerChallenge(new ItemDamageChallenge());
        challengeManager.registerChallenge(new SnakeChallenge());
        challengeManager.registerChallenge(new DietChallenge());
        challengeManager.registerChallenge(new NoDropChallenge());
        challengeManager.registerChallenge(new RandomDropChallenge());
        challengeManager.registerChallenge(new DamageFreezeChallenge());
        challengeManager.registerChallenge(new JumpMobChallenge());
        challengeManager.registerChallenge(new KillEffectChallenge());
        challengeManager.registerChallenge(new ChunkBlockBreakChallenge());
        challengeManager.registerChallenge(new LevelBorderChallenge());
        challengeManager.registerChallenge(new SpeedChallenge());
        challengeManager.registerChallenge(new ForceBiomeChallenge());
        challengeManager.registerChallenge(new ForceItemChallenge());
        challengeManager.registerChallenge(new ChunkEffectChallenge());
        challengeManager.registerChallenge(new OnlyDownChallenge());
        challengeManager.registerChallenge(new OnlyUpChallenge());
        challengeManager.registerChallenge(new NoSameHealthChallenge());
        challengeManager.registerChallenge(new OnlyMinecartChallenge());
        challengeManager.registerChallenge(new MobSwitchChallenge());
        challengeManager.registerChallenge(new MobDamageEffectChallenge());
        challengeManager.registerChallenge(new BiomeEffectChallenge());
        challengeManager.registerChallenge(new ForceItemBattleChallenge());
        challengeManager.registerChallenge(new LevelTeleportChallenge());

        // CacheManager als letztes initialisieren, da er andere Manager lädt
        this.cacheManager = new CacheManager();
        this.cacheManager.loadCache();

        // 3. Listener registrieren
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(), this);
        getServer().getPluginManager().registerEvents(new SettingsAndProjectListener(), this);

        // 4. Befehle registrieren
        CommandManager cmdManager = new CommandManager();
        String[] commands = {"settings", "challenges", "timer", "exclude", "position", "reset", "moboverview", "diet", "joker", "results", "skipitem", "invsee"};
        for (String cmd : commands) {
            var pluginCmd = getCommand(cmd);
            if (pluginCmd != null) {
                pluginCmd.setExecutor(cmdManager);
                pluginCmd.setTabCompleter(cmdManager);
            }
        }

        // Tab-HP initialisieren
        updateTabHP();

        // Ender-Partikel Task
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (timerManager.isRunning()) return;
                if (!settingsManager.getSetting(Setting.ENDER_PARTICLES)) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (settingsManager.isExcluded(player.getUniqueId())) continue;
                    if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;
                    
                    player.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, player.getLocation().add(0, 0.1, 0), 8, 0.2, 0.0, 0.2, 0.02);
                }
            }
        }.runTaskTimer(this, 10L, 10L);

        getLogger().info("ChallX erfolgreich geladen!");
    }

    @Override
    public void onDisable() {
        // Cache speichern
        if (cacheManager != null) {
            cacheManager.saveCache();
        }

        // Alle Challenges deaktivieren, um eventuelle Tasks zu stoppen
        if (challengeManager != null) {
            for (Challenge c : challengeManager.getChallenges()) {
                if (c.isEnabled()) {
                    c.setEnabled(false);
                }
            }
        }

        getLogger().info("ChallX erfolgreich entladen!");
    }

    public static ChallX getInstance() {
        return instance;
    }

    public ChallengeManager getChallengeManager() {
        return challengeManager;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public ThemeManager getThemeManager() {
        return themeManager;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public WorldResetManager getWorldResetManager() {
        return worldResetManager;
    }

    public ProjectManager getProjectManager() {
        return projectManager;
    }

    public PositionManager getPositionManager() {
        return positionManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    // --- GUI Menüs ---

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillBackground(CustomGUI gui, int rows) {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, "§7 ");
        for (int i = 0; i < rows * 9; i++) {
            if (gui.getButton(i) == null) {
                gui.setButton(i, new GUIButton(filler, e -> {}));
            }
        }
    }

    public void openSettingsGUI(Player player) {
        CustomGUI gui = new CustomGUI(Component.text("§6§lSettings / Challenges"), 3);

        // 1. Challenges Knopf (Slot 10)
        gui.setButton(10, new GUIButton(
                createItem(Material.NETHER_STAR, "§e§lChallenges", "§7Klicke, um Challenges ein-", "§7oder auszuschalten."),
                e -> openChallengesGUI(player)
        ));

        // 2. Projekte Knopf (Slot 12)
        gui.setButton(12, new GUIButton(
                createItem(Material.BOOK, "§a§lProjekte", "§7Klicke, um deine Projekte", "§7und deren Fortschritt zu sehen."),
                e -> openProjectsGUI(player)
        ));

        // 3. Einstellungen Knopf (Slot 14)
        gui.setButton(14, new GUIButton(
                createItem(Material.COMPARATOR, "§b§lEinstellungen", "§7Klicke, um globale Einstellungen", "§7(Shared Health, PVP, etc.) zu ändern."),
                e -> openGlobalSettingsGUI(player)
        ));

        // 4. Timer Knopf (Slot 16)
        gui.setButton(16, new GUIButton(
                createItem(Material.CLOCK, "§d§lTimer-Steuerung", "§7Klicke, um den Timer", "§7zu steuern."),
                e -> openTimerGUI(player)
        ));

        // 5. Themes Knopf (Slot 22)
        gui.setButton(22, new GUIButton(
                createItem(Material.PINK_DYE, "§d§lPlugin Themes", "§7Klicke, um exklusive Themes", "§7(CraftAttack 13, ZickZack v5, etc.) zu wählen."),
                e -> themeManager.openThemeGUI(player)
        ));

        fillBackground(gui, 3);
        gui.open(player);
    }

    public void openChallengesGUI(Player player) {
        openChallengesGUI(player, 0);
    }

    public void openChallengesGUI(Player player, int page) {
        CustomGUI gui = new CustomGUI(Component.text("§e§lChallenges (Seite " + (page + 1) + ")"), 6);

        // Back-Button
        gui.setButton(49, new GUIButton(
                createItem(Material.BARRIER, "§cZurück zum Hauptmenü"),
                e -> openSettingsGUI(player)
        ));

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 28, 29, 30, 31, 32, 33, 34};
        var challenges = challengeManager.getChallenges();

        int challengesPerPage = slots.length; // 14 pro Seite
        int totalPages = (int) Math.ceil((double) challenges.size() / (double) challengesPerPage);
        int startIdx = page * challengesPerPage;

        // Vorherige Seite (Slot 48)
        if (page > 0) {
            gui.setButton(48, new GUIButton(
                    createItem(Material.ARROW, "§e§lVorherige Seite"),
                    e -> openChallengesGUI(player, page - 1)
            ));
        }

        // Nächste Seite (Slot 50)
        if (page < totalPages - 1) {
            gui.setButton(50, new GUIButton(
                    createItem(Material.ARROW, "§e§lNächste Seite"),
                    e -> openChallengesGUI(player, page + 1)
            ));
        }

        for (int i = 0; i < challengesPerPage; i++) {
            int challengeIdx = startIdx + i;
            if (challengeIdx >= challenges.size()) break;

            Challenge c = challenges.get(challengeIdx);
            int slotIdx = slots[i];
            int statusSlotIdx = slotIdx + 9; // Slot direkt darunter

            // Icon vorbereiten
            ItemStack icon = c.getIcon().clone();
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                String statusStr = c.isEnabled() ? "§aAktiviert" : "§cDeaktiviert";
                String settingsStr = c.hasSettings() ? "§7[Rechtsklick: §cSub-Settings§7]" : "";
                meta.setLore(Arrays.asList(
                        "§7" + c.getDescription(),
                        "",
                        "§7Status: " + statusStr,
                        settingsStr
                ));
                icon.setItemMeta(meta);
            }

            GUIButton clickBtn = new GUIButton(icon, e -> {
                if (e.isRightClick() && c.hasSettings()) {
                    c.openSettings(player);
                } else {
                    c.setEnabled(!c.isEnabled());
                    openChallengesGUI(player, page); // GUI neu laden
                }
            });

            // Challenge-Knopf setzen
            gui.setButton(slotIdx, clickBtn);

            // Status-Knopf setzen
            Material paneMaterial = c.isEnabled() ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            String paneName = c.isEnabled() ? "§aAktiviert" : "§cDeaktiviert";
            gui.setButton(statusSlotIdx, new GUIButton(
                    createItem(paneMaterial, paneName, "§7Klicke zum Umschalten"),
                    e -> {
                        c.setEnabled(!c.isEnabled());
                        openChallengesGUI(player, page);
                    }
            ));
        }

        fillBackground(gui, 6);
        gui.open(player);
    }

    public void openGlobalSettingsGUI(Player player) {
        CustomGUI gui = new CustomGUI(Component.text("§b§lGlobale Einstellungen"), 6);

        gui.setButton(49, new GUIButton(
                createItem(Material.BARRIER, "§cZurück zum Hauptmenü"),
                e -> openSettingsGUI(player)
        ));

        Setting[] settings = {
                Setting.SHARED_HEARTS, Setting.ONE_LIFE_FOR_ALL, Setting.NATURAL_REGEN,
                Setting.CUT_CLEAN, Setting.DAMAGE_IN_CHAT, Setting.PVP, Setting.RESPAWN,
                Setting.SETTINGS_TITLE, Setting.MAX_HEALTH, Setting.PAUSE_ON_DAMAGE, Setting.START_ON_MOVE,
                Setting.UNNATURAL_REGEN, Setting.ENDER_PARTICLES, Setting.DEATH_COORDINATES, Setting.TAB_HP, Setting.BOSS_VICTORY
        };
        int[] slots = {1, 2, 3, 4, 5, 6, 7, 19, 20, 21, 22, 23, 24, 25, 37, 38};
        Material[] materials = {
                Material.HEART_OF_THE_SEA, // SHARED_HEARTS
                Material.TOTEM_OF_UNDYING, // ONE_LIFE_FOR_ALL
                Material.GOLDEN_APPLE,     // NATURAL_REGEN
                Material.IRON_INGOT,       // CUT_CLEAN
                Material.WRITABLE_BOOK,    // DAMAGE_IN_CHAT
                Material.IRON_SWORD,       // PVP
                Material.RED_BED,          // RESPAWN
                Material.NAME_TAG,         // SETTINGS_TITLE
                Material.APPLE,            // MAX_HEALTH
                Material.REDSTONE,         // PAUSE_ON_DAMAGE
                Material.FEATHER,          // START_ON_MOVE
                Material.GLISTERING_MELON_SLICE, // UNNATURAL_REGEN
                Material.ENDER_PEARL,      // ENDER_PARTICLES
                Material.COMPASS,          // DEATH_COORDINATES
                Material.PLAYER_HEAD,      // TAB_HP
                Material.NETHER_STAR       // BOSS_VICTORY
        };

        for (int i = 0; i < settings.length && i < slots.length; i++) {
            Setting s = settings[i];
            int slotIdx = slots[i];
            int statusSlotIdx = slotIdx + 9;

            if (s == Setting.MAX_HEALTH) {
                int hearts = settingsManager.getIntSetting(s);
                ItemStack icon = createItem(materials[i], "§b" + s.getDisplayName(), "§7" + s.getDescription(), "", "§7Aktueller Wert: §e" + hearts + " Herzen", "", "§7[Klicke für Einstellungen]");
                
                gui.setButton(slotIdx, new GUIButton(icon, e -> openMaxHealthGUI(player)));

                ItemStack valItem = createItem(Material.RED_STAINED_GLASS_PANE, "§e" + hearts + " Herzen", "§7Klicke zum Einstellen");
                gui.setButton(statusSlotIdx, new GUIButton(valItem, e -> openMaxHealthGUI(player)));
            } else {
                boolean enabled = settingsManager.getSetting(s);
                ItemStack icon = createItem(materials[i], "§b" + s.getDisplayName(), "§7" + s.getDescription(), "", "§7Status: " + (enabled ? "§aAktiviert" : "§cDeaktiviert"));

                gui.setButton(slotIdx, new GUIButton(icon, e -> {
                    boolean newVal = !settingsManager.getSetting(s);
                    settingsManager.setSetting(s, newVal);
                    if (s == Setting.TAB_HP) {
                        updateTabHP();
                    }
                    broadcastSettingsChange("§e" + s.getDisplayName() + " §7wurde " + (newVal ? "§aaktiviert" : "§cdeaktiviert") + ".");
                    openGlobalSettingsGUI(player);
                }));

                Material paneMaterial = enabled ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
                String paneName = enabled ? "§aAktiviert" : "§cDeaktiviert";
                gui.setButton(statusSlotIdx, new GUIButton(
                        createItem(paneMaterial, paneName, "§7Klicke zum Umschalten"),
                        e -> {
                            boolean newVal = !settingsManager.getSetting(s);
                            settingsManager.setSetting(s, newVal);
                            if (s == Setting.TAB_HP) {
                                updateTabHP();
                            }
                            broadcastSettingsChange("§e" + s.getDisplayName() + " §7wurde " + (newVal ? "§aaktiviert" : "§cdeaktiviert") + ".");
                            openGlobalSettingsGUI(player);
                        }
                ));
            }
        }

        fillBackground(gui, 6);
        gui.open(player);
    }

    public void openMaxHealthGUI(Player player) {
        CustomGUI gui = new CustomGUI(Component.text("§c§lMaximale Herzen"), 1);

        // -1 Herz (Slot 2)
        ItemStack minus = new ItemStack(Material.RED_WOOL);
        ItemMeta minusMeta = minus.getItemMeta();
        if (minusMeta != null) {
            minusMeta.setDisplayName("§c-1 Herz");
            minus.setItemMeta(minusMeta);
        }
        gui.setButton(2, new GUIButton(minus, e -> {
            int current = settingsManager.getIntSetting(Setting.MAX_HEALTH);
            if (current > 1) {
                settingsManager.setIntSetting(Setting.MAX_HEALTH, current - 1);
                updateMaxHealth();
                broadcastSettingsChange("§bMax. Herzen §7auf §e" + (current - 1) + " Herzen §7gesetzt.");
            }
            openMaxHealthGUI(player);
        }));

        // Info (Slot 4)
        int hearts = settingsManager.getIntSetting(Setting.MAX_HEALTH);
        ItemStack info = createItem(Material.HEART_OF_THE_SEA, "§eMaximale Herzen: §6" + hearts, "§7Setzt die Herzen aller Spieler.");
        gui.setButton(4, new GUIButton(info, e -> {}));

        // +1 Herz (Slot 6)
        ItemStack plus = new ItemStack(Material.GREEN_WOOL);
        ItemMeta plusMeta = plus.getItemMeta();
        if (plusMeta != null) {
            plusMeta.setDisplayName("§a+1 Herz");
            plus.setItemMeta(plusMeta);
        }
        gui.setButton(6, new GUIButton(plus, e -> {
            int current = settingsManager.getIntSetting(Setting.MAX_HEALTH);
            settingsManager.setIntSetting(Setting.MAX_HEALTH, current + 1);
            updateMaxHealth();
            broadcastSettingsChange("§bMax. Herzen §7auf §e" + (current + 1) + " Herzen §7gesetzt.");
            openMaxHealthGUI(player);
        }));

        // Zurück (Slot 8)
        gui.setButton(8, new GUIButton(
                createItem(Material.BARRIER, "§cZurück zu Einstellungen"),
                e -> openGlobalSettingsGUI(player)
        ));

        gui.open(player);
    }

    public void broadcastSettingsChange(String message) {
        if (settingsManager.getSetting(Setting.SETTINGS_TITLE)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("§bEinstellung geändert", message, 10, 40, 10);
            }
        }
    }

    public void updateMaxHealth() {
        double healthAmount = settingsManager.getIntSetting(Setting.MAX_HEALTH) * 2.0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (settingsManager.isExcluded(player.getUniqueId())) continue;
            var maxHealthAttr = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            if (maxHealthAttr != null) {
                maxHealthAttr.setBaseValue(healthAmount);
            }
        }
    }

    public void openProjectsGUI(Player player) {
        CustomGUI gui = new CustomGUI(Component.text("§a§lProjekte"), 3);

        gui.setButton(22, new GUIButton(
                createItem(Material.BARRIER, "§cZurück zum Hauptmenü"),
                e -> openSettingsGUI(player)
        ));

        // 1. Alle Mobs töten (Slot 10 / Status 19)
        {
            int killed = projectManager.getKilledMobs().size();
            int total = projectManager.getTargetMobs().size();
            boolean enabled = projectManager.isMobsEnabled();
            ItemStack item = createItem(
                    Material.ZOMBIE_HEAD, 
                    "§a§lAlle Mobs töten", 
                    "§7Töte jeden Mob-Typ in Minecraft.", 
                    "", 
                    "§7Status: " + (enabled ? "§aAktiviert" : "§cDeaktiviert"),
                    "§7Fortschritt: §e" + killed + " / " + total + " Mobs getötet.",
                    "",
                    "§7[Linksklick: §eToggeln§7]",
                    "§7[Rechtsklick: §aFortschritt im Chat§7]"
            );
            gui.setButton(10, new GUIButton(item, e -> {
                if (e.isRightClick()) {
                    player.closeInventory();
                    player.performCommand("moboverview");
                } else {
                    projectManager.setMobsEnabled(!projectManager.isMobsEnabled());
                    openProjectsGUI(player);
                }
            }));
            Material paneMaterial = enabled ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            String paneName = enabled ? "§aAktiviert" : "§cDeaktiviert";
            gui.setButton(19, new GUIButton(createItem(paneMaterial, paneName, "§7Klicke zum Umschalten"), e -> {
                projectManager.setMobsEnabled(!projectManager.isMobsEnabled());
                openProjectsGUI(player);
            }));
        }

        // 2. Alle Items sammeln (Slot 12 / Status 21)
        {
            int index = projectManager.getCurrentItemIndex();
            int total = projectManager.getTargetItems().size();
            boolean enabled = projectManager.isItemsEnabled();
            String currentItemName = index < total ? projectManager.getTargetItems().get(index).name() : "Keins";
            ItemStack item = createItem(
                    Material.GOLDEN_CARROT, 
                    "§a§lAlle Items sammeln", 
                    "§7Sammle alle Items in vorgegebener Reihenfolge.", 
                    "", 
                    "§7Status: " + (enabled ? "§aAktiviert" : "§cDeaktiviert"),
                    "§7Ziel-Item: §e" + currentItemName,
                    "§7Fortschritt: §e" + index + " / " + total + " Items gesammelt.",
                    "",
                    "§7[Linksklick: §eToggeln§7]"
            );
            gui.setButton(12, new GUIButton(item, e -> {
                projectManager.setItemsEnabled(!projectManager.isItemsEnabled());
                openProjectsGUI(player);
            }));
            Material paneMaterial = enabled ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            String paneName = enabled ? "§aAktiviert" : "§cDeaktiviert";
            gui.setButton(21, new GUIButton(createItem(paneMaterial, paneName, "§7Klicke zum Umschalten"), e -> {
                projectManager.setItemsEnabled(!projectManager.isItemsEnabled());
                openProjectsGUI(player);
            }));
        }

        // 3. Alle Todesnachrichten (Slot 14 / Status 23)
        {
            int index = projectManager.getCurrentDeathIndex();
            int total = projectManager.getTargetDeaths().size();
            boolean enabled = projectManager.isDeathsEnabled();
            String currentDeathName = index < total ? projectManager.getTargetDeaths().get(index).getDisplayName() : "Keins";
            ItemStack item = createItem(
                    Material.WITHER_SKELETON_SKULL, 
                    "§a§lAlle Todesnachrichten", 
                    "§7Erleide alle Todesursachen der Reihe nach.", 
                    "", 
                    "§7Status: " + (enabled ? "§aAktiviert" : "§cDeaktiviert"),
                    "§7Ziel-Tod: §e" + currentDeathName,
                    "§7Fortschritt: §e" + index + " / " + total + " Tode erlitten.",
                    "",
                    "§7[Linksklick: §eToggeln§7]"
            );
            gui.setButton(14, new GUIButton(item, e -> {
                projectManager.setDeathsEnabled(!projectManager.isDeathsEnabled());
                openProjectsGUI(player);
            }));
            Material paneMaterial = enabled ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            String paneName = enabled ? "§aAktiviert" : "§cDeaktiviert";
            gui.setButton(23, new GUIButton(createItem(paneMaterial, paneName, "§7Klicke zum Umschalten"), e -> {
                projectManager.setDeathsEnabled(!projectManager.isDeathsEnabled());
                openProjectsGUI(player);
            }));
        }

        // 4. Alle Achievements (Slot 16 / Status 25)
        {
            int completed = projectManager.getCompletedAchievements().size();
            int total = projectManager.getMinecraftAdvancementCount();
            boolean enabled = projectManager.isAchievementsEnabled();
            ItemStack item = createItem(
                    Material.KNOWLEDGE_BOOK, 
                    "§a§lAlle Achievements", 
                    "§7Schalte alle Minecraft Achievements frei.", 
                    "", 
                    "§7Status: " + (enabled ? "§aAktiviert" : "§cDeaktiviert"),
                    "§7Fortschritt: §e" + completed + " / " + total + " freigeschaltet.",
                    "",
                    "§7[Linksklick: §eToggeln§7]"
            );
            gui.setButton(16, new GUIButton(item, e -> {
                projectManager.setAchievementsEnabled(!projectManager.isAchievementsEnabled());
                openProjectsGUI(player);
            }));
            Material paneMaterial = enabled ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            String paneName = enabled ? "§aAktiviert" : "§cDeaktiviert";
            gui.setButton(25, new GUIButton(createItem(paneMaterial, paneName, "§7Klicke zum Umschalten"), e -> {
                projectManager.setAchievementsEnabled(!projectManager.isAchievementsEnabled());
                openProjectsGUI(player);
            }));
        }

        fillBackground(gui, 3);
        gui.open(player);
    }

    public void openTimerGUI(Player player) {
        CustomGUI gui = new CustomGUI(Component.text("§d§lTimer-Steuerung"), 3);

        gui.setButton(22, new GUIButton(
                createItem(Material.BARRIER, "§cZurück zum Hauptmenü"),
                e -> openSettingsGUI(player)
        ));

        // Start (Slot 10)
        gui.setButton(10, new GUIButton(
                createItem(Material.LIME_CONCRETE, "§a§lStart / Fortsetzen", "§7Startet den Timer."),
                e -> {
                    timerManager.start();
                    player.sendMessage("§aTimer gestartet.");
                    openTimerGUI(player);
                }
        ));

        // Pause (Slot 12)
        gui.setButton(12, new GUIButton(
                createItem(Material.YELLOW_CONCRETE, "§e§lPausieren", "§7Pausiert den Timer."),
                e -> {
                    timerManager.pause();
                    player.sendMessage("§cTimer pausiert.");
                    openTimerGUI(player);
                }
        ));

        // Reset (Slot 14)
        gui.setButton(14, new GUIButton(
                createItem(Material.RED_CONCRETE, "§c§lZurücksetzen", "§7Setzt den Timer auf 0 zurück."),
                e -> {
                    timerManager.reset();
                    player.sendMessage("§eTimer zurückgesetzt.");
                    openTimerGUI(player);
                }
        ));

        // Reverse Toggle (Slot 16)
        boolean isRev = timerManager.isReverse();
        ItemStack revItem = createItem(
                Material.ORANGE_CONCRETE, 
                "§6§lRückwärtslauf", 
                "§7Lässt den Timer rückwärts laufen.", 
                "", 
                "§7Status: " + (isRev ? "§aAktiviert" : "§cDeaktiviert")
        );
        gui.setButton(16, new GUIButton(revItem, e -> {
            timerManager.setReverse(!timerManager.isReverse());
            player.sendMessage("§6Timer-Rückwärtslauf: " + (timerManager.isReverse() ? "§aAktiviert" : "§cDeaktiviert"));
            openTimerGUI(player);
        }));

        fillBackground(gui, 3);
        gui.open(player);
    }

    public void updateTabHP() {
        boolean enabled = settingsManager.getSetting(Setting.TAB_HP);
        org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Objective obj = sb.getObjective("health_tab");
        
        if (enabled) {
            if (obj == null) {
                obj = sb.registerNewObjective("health_tab", org.bukkit.scoreboard.Criteria.HEALTH, Component.text("§c❤"), org.bukkit.scoreboard.RenderType.HEARTS);
            }
            obj.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.PLAYER_LIST);
        } else {
            if (obj != null) {
                obj.unregister();
            }
        }
    }
}
