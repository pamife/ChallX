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

        // 1. Temporäre Lobby-Welt erstellen und Spieler dorthin teleportieren
        World lobby = createLobbyWorld();
        Location lobbySpawn = new Location(lobby, 0.5, 100, 0.5);
        lobbySpawn.getBlock().setType(Material.GLASS); // Spawn-Plattform erstellen

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isDead()) {
                player.spigot().respawn();
            }
            player.teleport(lobbySpawn);
            player.sendMessage(ChatColor.GOLD + "Die Welten werden zurückgesetzt...");
        }

        // Verzögerung, um Teleportationen abzuschließen
        new BukkitRunnable() {
            @Override
            public void run() {
                // 2. Welten entladen
                unloadWorld("world_nether");
                unloadWorld("world_the_end");

                World overworld = Bukkit.getWorld("world");
                if (overworld != null) {
                    for (Player p : overworld.getPlayers()) {
                        if (p.isDead()) {
                            p.spigot().respawn();
                        }
                        p.teleport(lobbySpawn);
                    }
                    overworld.setKeepSpawnInMemory(false);
                    for (Chunk chunk : overworld.getLoadedChunks()) {
                        chunk.unload(false);
                    }
                }

                // 3. Welten-Dateien löschen
                deleteWorldFolder(new File("world_nether"));
                deleteWorldFolder(new File("world_the_end"));
                deleteMainWorldContents();

                // 4. Welten neu generieren (nach einer kurzen Pause, damit Dateisperren gelöst werden)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.createWorld(new WorldCreator("world_nether").environment(World.Environment.NETHER));
                        Bukkit.createWorld(new WorldCreator("world_the_end").environment(World.Environment.THE_END));

                        World newOverworld = Bukkit.getWorld("world");
                        if (newOverworld == null) {
                            newOverworld = Bukkit.createWorld(new WorldCreator("world"));
                        }

                        // Chunk laden und generieren
                        Location spawnLoc = newOverworld.getSpawnLocation();
                        newOverworld.getChunkAt(spawnLoc).load(true);

                        // Sichere Y-Koordinate bestimmen (Verhindert Void-Spawning)
                        int highestY = newOverworld.getHighestBlockYAt(spawnLoc);
                        if (highestY < -64) {
                            highestY = 64; // Fallback
                        }
                        spawnLoc.setY(highestY + 1);

                        // Spawnplattform in der Hauptwelt erstellen
                        for (int x = -2; x <= 2; x++) {
                            for (int z = -2; z <= 2; z++) {
                                spawnLoc.clone().add(x, -1, z).getBlock().setType(Material.GLASS);
                            }
                        }

                        // Timer und Einstellungen zurücksetzen
                        ChallX.getInstance().getTimerManager().reset();

                        // Spieler zurückteleportieren
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.setGameMode(GameMode.SURVIVAL); // Zurück in Survival
                            player.teleport(spawnLoc);
                            player.sendMessage(ChatColor.GREEN + "Welten erfolgreich zurückgesetzt!");
                        }

                        resetting = false;
                    }
                }.runTaskLater(ChallX.getInstance(), 40L);
            }
        }.runTaskLater(ChallX.getInstance(), 20L);
    }

    private World createLobbyWorld() {
        World lobby = Bukkit.getWorld("challx_lobby");
        if (lobby == null) {
            WorldCreator creator = new WorldCreator("challx_lobby");
            creator.type(WorldType.FLAT);
            creator.generatorSettings("{\"layers\": [{\"block\": \"minecraft:air\", \"height\": 1}], \"biome\": \"minecraft:the_void\"}");
            creator.generateStructures(false);
            lobby = Bukkit.createWorld(creator);
        }
        return lobby;
    }

    private void unloadWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            for (Player p : world.getPlayers()) {
                p.teleport(Bukkit.getWorld("challx_lobby").getSpawnLocation());
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
