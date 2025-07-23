package io.github.nikorr0.nexusCoreTeams.listeners;

import io.github.nikorr0.nexusCoreTeams.NexusManager;
import io.github.nikorr0.nexusCoreTeams.util.TeamColors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Team;

public class PlayerRespawnListener implements Listener {

    private final NexusManager manager;

    public PlayerRespawnListener(NexusManager manager) {
        this.manager = manager;
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (!manager.getPlugin().isActive()) return;

        Player player = e.getEntity();
        Team team = player.getScoreboard().getPlayerTeam(player);
        if (team == null) return;

        Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> {
            if (!manager.isTeamAlive(team.getName())) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(ChatColor.GRAY + "Your team's Nexus has been destroyed! Now you are a spectator.");
            }

            // If the team's nexus is still alive, then we don't notify
            if (manager.isTeamAlive(team.getName())) return;

            // Are there any other players still ALIVE in the team?
            for (OfflinePlayer op : team.getPlayers()) {
                Player p = op.getPlayer();
                if (p != null && p.getGameMode() != GameMode.SPECTATOR) {
                    return;
                }
            }

            // No survivors -> announce elimination
            ChatColor cc = TeamColors.get(team.getName());
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("§cThe " + cc + team.getName() + " §cteam is out of the game!");
            }
        }, 1L);
    }
}
