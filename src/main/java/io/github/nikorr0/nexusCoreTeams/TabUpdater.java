package io.github.nikorr0.nexusCoreTeams;

import io.github.nikorr0.nexusCoreTeams.util.TeamColors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class TabUpdater {

    private final NexusCoreTeams plugin;
    private final NexusManager manager;
    private BukkitTask task;

    public TabUpdater(NexusCoreTeams plugin, NexusManager manager) {
        this.plugin  = plugin;
        this.manager = manager;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(
                plugin, this::updateAll, 1L, 40L); // 2 seconds
    }

    public void stop() {
        if (task != null) task.cancel();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setPlayerListHeaderFooter("", "");  // empty TAB footer
        }
    }

    // Main TAB update
    private void updateAll() {
        StringBuilder header = new StringBuilder(ChatColor.GOLD + "--- Nexuses ---\n");
        for (TeamData td : manager.getTeamDataMap().values()) {
            ChatColor clr = TeamColors.get(td.getTeamName());
            header.append(clr).append(td.getTeamName())
                    .append(ChatColor.WHITE + ": ").append(td.isAlive() ? ChatColor.WHITE + ""
                            + td.getHealth() + " HP" : ChatColor.RED + "☠")
                    .append('\n');
        }

        header.append(ChatColor.GOLD + "---------------");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setPlayerListHeader(header.toString());
        }

    }

    public void updateNow() {
        updateAll();
    }
}
