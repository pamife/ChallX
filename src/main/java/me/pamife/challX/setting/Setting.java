package me.pamife.challX.setting;

public enum Setting {
    SHARED_HEARTS("Geteilte Herzen", "Alle Spieler teilen sich die Herzen."),
    ONE_LIFE_FOR_ALL("Ein Leben für alle", "Stirbt ein Spieler, scheitert die Challenge."),
    NATURAL_REGEN("Natürliche Regeneration", "Toggelt die natürliche Regeneration."),
    CUT_CLEAN("Cut Clean", "Erze und Fleisch droppen direkt geschmolzen/gebraten."),
    DAMAGE_IN_CHAT("Schaden im Chat", "Gibt Schaden von Spielern im Chat aus."),
    PVP("PVP", "Aktiviert oder deaktiviert PVP zwischen Spielern."),
    RESPAWN("Respawn", "Bestimmt, ob Spieler normal respawnen können."),
    MAX_HEALTH("Maximale Herzen", "Setzt das maximale Leben aller Spieler."),
    SETTINGS_TITLE("Titel bei Änderungen", "Zeigt einen Bildschirm-Titel bei Einstellungsänderungen an."),
    PAUSE_ON_DAMAGE("Pause bei Schaden", "Pausiert den Timer automatisch, wenn ein Spieler Schaden erleidet."),
    START_ON_MOVE("Start bei Bewegung", "Startet den Timer automatisch, wenn sich ein Spieler bewegt."),
    UNNATURAL_REGEN("Unnatürliche Regeneration", "Spieler können auf unnatürliche Art (Tränke, Goldäpfel) regenerieren."),
    ENDER_PARTICLES("Ender-Partikel", "Spieler haben Partikel unter ihren Füßen, während der Timer pausiert ist."),
    DEATH_COORDINATES("Todeskoordinaten", "Zeigt die Koordinaten hinter der Todesnachricht an."),
    TAB_HP("Tab-HP", "Zeigt die Herzen der Spieler in der Tab-Liste an."),
    BOSS_VICTORY("Boss-Sieg", "Challenge ist absolviert, wenn der Enderdrache oder Wither besiegt wird.");

    private final String displayName;
    private final String description;

    Setting(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
