package me.pamife.challX.manager;

import me.pamife.challX.ChallX;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class WorldResetManager {

    private boolean resetting = false;

    public void resetWorlds() {
        if (resetting) return;
        resetting = true;

        // 1. Hinweis an alle Spieler senden
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§c§lRESET ANGEFORDERT", "§eBitte starte den Server neu!", 10, 100, 20);
            player.sendMessage(" ");
            player.sendMessage("§c§l[Reset] §eEin Welten-Reset wurde angefordert!");
            player.sendMessage("§c§l[Reset] §7Die Nether- und End-Welten wurden zurückgesetzt.");
            player.sendMessage("§c§l[Reset] §eBitte starte den Server jetzt neu, damit die Hauptwelt neu generiert wird.");
            player.sendMessage(" ");
        }

        // 2. Timer pausieren und zurücksetzen
        ChallX.getInstance().getTimerManager().pause();
        ChallX.getInstance().getTimerManager().reset();

        // 3. Nether und End entladen und löschen (da diese entladen werden können)
        unloadWorld("world_nether");
        unloadWorld("world_the_end");

        new BukkitRunnable() {
            @Override
            public void run() {
                deleteWorldFolder(new File("world_nether"));
                deleteWorldFolder(new File("world_the_end"));
                
                // Hauptwelt-region Ordner versuchen zu löschen (falls möglich, sonst wird es beim Restart vom OS/Skript geregelt)
                deleteMainWorldContents();

                resetting = false;
            }
        }.runTaskLater(ChallX.getInstance(), 20L);
    }

    private void unloadWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            World overworld = Bukkit.getWorld("world");
            Location spawn = overworld != null ? overworld.getSpawnLocation() : new Location(Bukkit.getWorlds().get(0), 0.5, 100, 0.5);
            for (Player p : world.getPlayers()) {
                p.teleport(spawn);
            }
            Bukkit.unloadWorld(world, false);
        }
    }

    private void deleteWorldFolder(File path) {
        if (!path.exists()) return;
        try {
            Files.walk(path.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException e) {
            ChallX.getInstance().getLogger().warning("Konnte Weltordner nicht löschen: " + path.getName());
        }
    }

    private void deleteMainWorldContents() {
        String[] subDirs = {"region", "poi", "data", "entities"};
        File mainWorldFolder = new File("world");
        if (!mainWorldFolder.exists()) return;
        for (String subDirName : subDirs) {
            File subDir = new File(mainWorldFolder, subDirName);
            if (subDir.exists()) {
                deleteWorldFolder(subDir);
            }
        }
    }
}
