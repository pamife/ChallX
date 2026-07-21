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

        // CacheManager als letztes initialisieren, da er andere Manager lädt
        this.cacheManager = new CacheManager();
        this.cacheManager.loadCache();

        // 3. Listener registrieren
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(), this);
        getServer().getPluginManager().registerEvents(new SettingsAndProjectListener(), this);

        // 4. Befehle registrieren
        CommandManager cmdManager = new CommandManager();
        String[] commands = {"settings", "challenges", "timer", "exclude", "position", "reset", "moboverview", "diet", "joker", "results", "skipitem"};
        for (String cmd : commands) {
            var pluginCmd = getCommand(cmd);
            if (pluginCmd != null) {
                pluginCmd.setExecutor(cmdManager);
                pluginCmd.setTabCompleter(cmdManager);
            }
        }

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

        fillBackground(gui, 3);
        gui.open(player);
    }

    public void openChallengesGUI(Player player) {
        CustomGUI gui = new CustomGUI(Component.text("§e§lChallenges"), 5);

        // Back-Button
        gui.setButton(40, new GUIButton(
                createItem(Material.BARRIER, "§cZurück zum Hauptmenü"),
                e -> openSettingsGUI(player)
        ));

        int[] slots = {10, 11, 12, 13, 14, 15};
        var challenges = challengeManager.getChallenges();

        for (int i = 0; i < challenges.size() && i < slots.length; i++) {
            Challenge c = challenges.get(i);
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
                    openChallengesGUI(player); // GUI neu laden
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
                        openChallengesGUI(player);
                    }
            ));
        }

        fillBackground(gui, 5);
        gui.open(player);
    }

    public void openGlobalSettingsGUI(Player player) {
        CustomGUI gui = new CustomGUI(Component.text("§b§lGlobale Einstellungen"), 5);

        gui.setButton(40, new GUIButton(
                createItem(Material.BARRIER, "§cZurück zum Hauptmenü"),
                e -> openSettingsGUI(player)
        ));

        Setting[] settings = Setting.values();
        int[] slots = {10, 11, 12, 13, 14, 15};
        Material[] materials = {
                Material.HEART_OF_THE_SEA, // SHARED_HEARTS
                Material.TOTEM_OF_UNDYING, // ONE_LIFE_FOR_ALL
                Material.GOLDEN_APPLE,     // NATURAL_REGEN
                Material.IRON_INGOT,       // CUT_CLEAN
                Material.WRITABLE_BOOK,    // DAMAGE_IN_CHAT
                Material.IRON_SWORD        // PVP
        };

        for (int i = 0; i < settings.length && i < slots.length; i++) {
            Setting s = settings[i];
            int slotIdx = slots[i];
            int statusSlotIdx = slotIdx + 9;
            boolean enabled = settingsManager.getSetting(s);

            ItemStack icon = createItem(materials[i], "§b" + s.getDisplayName(), "§7" + s.getDescription(), "", "§7Status: " + (enabled ? "§aAktiviert" : "§cDeaktiviert"));

            GUIButton clickBtn = new GUIButton(icon, e -> {
                settingsManager.setSetting(s, !settingsManager.getSetting(s));
                openGlobalSettingsGUI(player);
            });

            gui.setButton(slotIdx, clickBtn);

            Material paneMaterial = enabled ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            String paneName = enabled ? "§aAktiviert" : "§cDeaktiviert";
            gui.setButton(statusSlotIdx, new GUIButton(
                    createItem(paneMaterial, paneName, "§7Klicke zum Umschalten"),
                    e -> {
                        settingsManager.setSetting(s, !settingsManager.getSetting(s));
                        openGlobalSettingsGUI(player);
                    }
            ));
        }

        fillBackground(gui, 5);
        gui.open(player);
    }

    public void openProjectsGUI(Player player) {
        CustomGUI gui = new CustomGUI(Component.text("§a§lProjekte"), 3);

        gui.setButton(22, new GUIButton(
                createItem(Material.BARRIER, "§cZurück zum Hauptmenü"),
                e -> openSettingsGUI(player)
        ));

        int killed = projectManager.getKilledMobs().size();
        int total = projectManager.getTargetMobs().size();
        boolean enabled = projectManager.isEnabled();

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

        gui.setButton(11, new GUIButton(item, e -> {
            if (e.isRightClick()) {
                player.closeInventory();
                player.performCommand("moboverview");
            } else {
                projectManager.setEnabled(!projectManager.isEnabled());
                openProjectsGUI(player);
            }
        }));

        Material paneMaterial = enabled ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String paneName = enabled ? "§aAktiviert" : "§cDeaktiviert";
        gui.setButton(20, new GUIButton(
                createItem(paneMaterial, paneName, "§7Klicke zum Umschalten"),
                e -> {
                    projectManager.setEnabled(!projectManager.isEnabled());
                    openProjectsGUI(player);
                }
        ));

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
}
