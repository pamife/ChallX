package me.pamife.challX.manager;

import me.pamife.challX.ChallX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TimerManager {
    private int time = 0;
    private boolean running = false;
    private boolean reverse = false;

    public TimerManager() {
        // Kontinuierlicher Task zur Aktualisierung der Actionbar und der Zeit
        new BukkitRunnable() {
            @Override
            public void run() {
                if (running) {
                    if (reverse) {
                        if (time > 0) {
                            time--;
                        } else {
                            running = false;
                            broadcastMessage("<red><bold>Der Timer ist abgelaufen!</bold></red>");
                        }
                    } else {
                        time++;
                    }
                }
                sendActionBar();
            }
        }.runTaskTimer(ChallX.getInstance(), 20L, 20L);
    }

    public void start() {
        running = true;
    }

    public void pause() {
        running = false;
    }

    public void reset() {
        running = false;
        time = 0;
        reverse = false;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public void sendActionBar() {
        String message;
        if (running) {
            int h = time / 3600;
            int m = (time % 3600) / 60;
            int s = time % 60;
            String timeStr = h > 0 ? String.format("%02d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
            message = "<gradient:gold:yellow><bold>" + timeStr + "</bold></gradient>";
        } else {
            message = "<red><bold>Timer pausiert</bold></red>";
        }
        Component component = MiniMessage.miniMessage().deserialize(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(component);
        }
    }

    private void broadcastMessage(String miniMessageFormat) {
        Component comp = MiniMessage.miniMessage().deserialize(miniMessageFormat);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(comp);
        }
    }
}
