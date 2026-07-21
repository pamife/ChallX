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
    SETTINGS_TITLE("Titel bei Änderungen", "Zeigt einen Bildschirm-Titel bei Einstellungsänderungen an.");

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
