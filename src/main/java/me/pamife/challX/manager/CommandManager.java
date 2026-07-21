package me.pamife.challX.manager;

import me.pamife.challX.ChallX;
import me.pamife.challX.setting.Setting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern ausgeführt werden.");
            return true;
        }

        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "settings":
            case "challenges":
                ChallX.getInstance().openSettingsGUI(player);
                return true;

            case "timer":
                return handleTimer(player, args);

            case "exclude":
                return handleExclude(player, args);

            case "position":
                return handlePosition(player, args);

            case "reset":
                if (!player.hasPermission("challenges.reset")) {
                    player.sendMessage("§cDu hast keine Rechte dafür.");
                    return true;
                }
                ChallX.getInstance().getWorldResetManager().resetWorlds();
                return true;

            case "moboverview":
                return handleMobOverview(player);

            // Dummys/Placeholders
            case "diet":
                player.sendMessage("§cDie Diät-Challenge ist momentan nicht aktiv.");
                return true;
            case "joker":
                player.sendMessage("§cKein Force-Item Battle aktiv.");
                return true;
            case "results":
                player.sendMessage("§cKeine Ergebnisse verfügbar.");
                return true;
            case "skipitem":
                player.sendMessage("§cKein All-Items Projekt aktiv.");
                return true;
        }

        return false;
    }

    private boolean handleTimer(Player player, String[] args) {
        if (!player.hasPermission("challenges.timer")) {
            player.sendMessage("§cDu hast keine Rechte dafür.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cVerwendung: /timer <resume|pause|reset|reverse|set|settings>");
            return true;
        }

        TimerManager tm = ChallX.getInstance().getTimerManager();
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "resume":
                tm.start();
                Bukkit.broadcastMessage("§aDer Timer wurde fortgesetzt.");
                break;
            case "pause":
                tm.pause();
                Bukkit.broadcastMessage("§cDer Timer wurde pausiert.");
                break;
            case "reset":
                tm.reset();
                Bukkit.broadcastMessage("§eDer Timer wurde zurückgesetzt.");
                break;
            case "reverse":
                tm.setReverse(!tm.isReverse());
                Bukkit.broadcastMessage("§6Timer-Rückwärtslauf: " + (tm.isReverse() ? "§aAktiviert" : "§cDeaktiviert"));
                break;
            case "settings":
                ChallX.getInstance().openTimerGUI(player);
                break;
            case "set":
                if (args.length < 2) {
                    player.sendMessage("§cBitte gib eine Zeit an (z.B. /timer set 2h 30m oder /timer set 600)");
                    return true;
                }
                // Argumente zusammenfügen
                StringBuilder timeString = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    timeString.append(args[i]).append(" ");
                }
                int seconds = parseTime(timeString.toString().trim());
                tm.setTime(seconds);
                Bukkit.broadcastMessage("§eTimer-Zeit gesetzt auf: §6" + formatSeconds(seconds));
                break;
            default:
                player.sendMessage("§cUnbekannter Befehl. Verwendung: /timer <resume|pause|reset|reverse|set|settings>");
        }
        return true;
    }

    private boolean handleExclude(Player player, String[] args) {
        if (!player.hasPermission("challenges.exclude")) {
            player.sendMessage("§cDu hast keine Rechte dafür.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cVerwendung: /exclude <add|remove|get> <spieler>");
            return true;
        }

        String sub = args[0].toLowerCase();
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        UUID targetUUID = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(targetName).getUniqueId();
        SettingsManager sm = ChallX.getInstance().getSettingsManager();

        switch (sub) {
            case "add":
                sm.excludePlayer(targetUUID);
                player.sendMessage("§aSpieler §e" + targetName + " §awurde von Challenges ausgeschlossen.");
                if (target != null) {
                    target.sendMessage("§eDu wurdest von den Challenges ausgeschlossen.");
                }
                break;
            case "remove":
                sm.includePlayer(targetUUID);
                player.sendMessage("§aSpieler §e" + targetName + " §animmt nun wieder an Challenges teil.");
                if (target != null) {
                    target.sendMessage("§eDu nimmst wieder an den Challenges teil.");
                }
                break;
            case "get":
                boolean excluded = sm.isExcluded(targetUUID);
                player.sendMessage("§7Status von §e" + targetName + "§7: " + (excluded ? "§cAusgeschlossen" : "§aTeilnehmer"));
                break;
            default:
                player.sendMessage("§cVerwendung: /exclude <add|remove|get> <spieler>");
        }
        return true;
    }

    private boolean handlePosition(Player player, String[] args) {
        if (!player.hasPermission("challenges.position")) {
            player.sendMessage("§cDu hast keine Rechte dafür.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cVerwendung: /position <name|delete|share|list> [name]");
            return true;
        }

        PositionManager pm = ChallX.getInstance().getPositionManager();
        String sub = args[0].toLowerCase();

        if (sub.equals("list")) {
            if (pm.getPositions().isEmpty()) {
                player.sendMessage("§cEs sind keine Positionen gespeichert.");
                return true;
            }
            player.sendMessage("§a--- Gespeicherte Positionen ---");
            for (Map.Entry<String, Location> entry : pm.getPositions().entrySet()) {
                Location loc = entry.getValue();
                player.sendMessage("§e" + entry.getKey() + "§7: §fX: " + loc.getBlockX() + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ() + " (" + loc.getWorld().getName() + ")");
            }
            return true;
        }

        if (sub.equals("share")) {
            Location loc = player.getLocation();
            Bukkit.broadcastMessage("§a[Position] §e" + player.getName() + " §7teilt Koordinaten: §fX: " + loc.getBlockX() + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ() + " (" + loc.getWorld().getName() + ")");
            return true;
        }

        if (sub.equals("delete")) {
            if (args.length < 2) {
                player.sendMessage("§cVerwendung: /position delete <name>");
                return true;
            }
            String name = args[1];
            if (pm.deletePosition(name)) {
                player.sendMessage("§aPosition §e" + name + " §awurde gelöscht.");
            } else {
                player.sendMessage("§cEs wurde keine Position mit dem Namen §e" + name + " §cgefunden.");
            }
            return true;
        }

        // /position <name>
        String name = sub;
        Location existing = pm.getPosition(name);
        if (existing != null) {
            player.sendMessage("§aPosition §e" + name + "§7: §fX: " + existing.getBlockX() + " Y: " + existing.getBlockY() + " Z: " + existing.getBlockZ() + " (" + existing.getWorld().getName() + ")");
        } else {
            pm.savePosition(name, player.getLocation());
            player.sendMessage("§aAktuelle Position als §e" + name + " §agespeichert.");
        }
        return true;
    }

    private boolean handleMobOverview(Player player) {
        ProjectManager pm = ChallX.getInstance().getProjectManager();
        Set<EntityType> remaining = pm.getRemainingMobs();
        
        if (remaining.isEmpty()) {
            player.sendMessage("§a[Projekt] Alle Mobs wurden getötet!");
            return true;
        }

        player.sendMessage("§a--- Verbleibende Mobs (" + remaining.size() + "/" + pm.getTargetMobs().size() + ") ---");
        String names = remaining.stream()
                .map(EntityType::name)
                .map(String::toLowerCase)
                .collect(Collectors.joining("§7, §6"));
        player.sendMessage("§6" + names);
        return true;
    }

    private int parseTime(String timeStr) {
        int seconds = 0;
        String[] parts = timeStr.toLowerCase().split("\\s+");
        for (String part : parts) {
            try {
                if (part.endsWith("h")) {
                    seconds += Integer.parseInt(part.replace("h", "")) * 3600;
                } else if (part.endsWith("m")) {
                    seconds += Integer.parseInt(part.replace("m", "")) * 60;
                } else if (part.endsWith("s")) {
                    seconds += Integer.parseInt(part.replace("s", ""));
                } else {
                    seconds += Integer.parseInt(part);
                }
            } catch (NumberFormatException ignored) {}
        }
        return seconds;
    }

    private String formatSeconds(int totalSeconds) {
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;
        if (h > 0) {
            return String.format("%d Std. %d Min. %d Sek.", h, m, s);
        } else if (m > 0) {
            return String.format("%d Min. %d Sek.", m, s);
        } else {
            return String.format("%d Sek.", s);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        String cmdName = command.getName().toLowerCase();

        if (cmdName.equals("timer") && args.length == 1) {
            list.addAll(Arrays.asList("resume", "pause", "reset", "reverse", "set", "settings"));
        } else if (cmdName.equals("exclude") && args.length == 1) {
            list.addAll(Arrays.asList("add", "remove", "get"));
        } else if (cmdName.equals("exclude") && args.length == 2) {
            list.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        } else if (cmdName.equals("position") && args.length == 1) {
            list.addAll(Arrays.asList("delete", "share", "list"));
        } else if (cmdName.equals("position") && args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            list.addAll(ChallX.getInstance().getPositionManager().getPositions().keySet());
        }

        String lastArg = args[args.length - 1].toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(lastArg)).collect(Collectors.toList());
    }
}
