package io.github.nikorr0.nexusCoreTeams.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public final class TeamColors {

    private TeamColors() {}

    public static ChatColor get(String teamName) {
        var t = Bukkit.getScoreboardManager()
                .getMainScoreboard()
                .getTeam(teamName);

        return t != null ? t.getColor() : ChatColor.WHITE;
    }


}