package io.github.nikorr0.nexusCoreTeams.commands;

import io.github.nikorr0.nexusCoreTeams.NexusCoreTeams;
import io.github.nikorr0.nexusCoreTeams.NexusManager;
import io.github.nikorr0.nexusCoreTeams.TeamData;
import io.github.nikorr0.nexusCoreTeams.util.TeamColors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NexusCommand implements CommandExecutor, TabCompleter {

    private final NexusManager manager;
    private final NexusCoreTeams plugin;

    public NexusCommand(NexusManager manager, NexusCoreTeams plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, String[] args) {
        if (args.length == 0) return false;


        switch (args[0].toLowerCase()) {
            case "enable":   return handleToggle(sender, true);
            case "disable":  return handleToggle(sender, false);
        }

        if (!manager.getPlugin().isActive()) return false;

        switch (args[0].toLowerCase()) {
            case "set":      return handleSet(sender, args);
            case "hp":       return handleHp(sender, args);
            case "info":     return handleInfo(sender, args);
            case "clear":    return handleClear(sender, args);
            case "reload":   return handleReload(sender);
            default:         return false;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("nexusteams.admin")) {
            sender.sendMessage("§cYou don't have permissions to execute this command.");
            return true;
        }
        plugin.reloadPluginConfig(sender);
        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p) || args.length < 2) return false;

        String team = args[1];

        if (manager.hasNexus(team)) {
            sender.sendMessage("§cThe §e" + team + " §cteam has the Nexus already installed! "
                    + "Clear it via §e/nexus clear <team> "
                    + "§cor set HP to the existing Nexus via §e/nexus hp <team> <value>.");
            return true;
        }

        int hp = 1;

        try{
            hp = (args.length >= 3)
                    ? Integer.parseInt(args[2])
                    : manager.getPlugin()
                    .getConfig()
                    .getInt("default-nexus-hp", 10);
        } catch (Throwable t) {
            sender.sendMessage("§cWrong value of HP.");
            return false;
        }

        Location loc = p.getLocation().getBlock().getLocation();
        manager.registerNexus(team, loc, hp);

        sender.sendMessage("§aThe §e" + team + " §ateam Nexus has been registered. HP = " + hp);
        return true;
    }

    private boolean handleHp(CommandSender sender, String[] args) {
        if (args.length != 3) return false;

        String team = args[1];
        int hp = 1;

        try{
            hp = Integer.parseInt(args[2]);
        } catch (Throwable t) {
            sender.sendMessage("§cWrong value of HP.");
            return false;
        }

        TeamData td = manager.getTeamData(team);
        if (td == null) {
            sender.sendMessage("§cThere is no such a team.");
            return true;
        }
        td.setHealth(hp);
        manager.saveAll();
        sender.sendMessage("§aThe §e" + team + " §ateam Nexus HP set to " + hp);
        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length == 1) {
            manager.getTeamDataMap().forEach((name, data) ->
                    sender.sendMessage( TeamColors.get(name) + name + ChatColor.WHITE + ": HP " +
                            data.getHealth()));
            return true;
        }
        String team = args[1];
        TeamData td = manager.getTeamData(team);
        ChatColor cc = TeamColors.get(team);

        if (td == null) {
            sender.sendMessage("§cThere is no such a team.");
            return true;
        }
        sender.sendMessage(cc + team + "§7: " + td.getHealth() + " HP");
        return true;
    }

    private boolean handleClear(CommandSender sender, String[] args) {

        if (!sender.hasPermission("nexusteams.admin")) {
            sender.sendMessage("§cYou don't have permissions to execute this command.");
            return true;
        }

        // On command /nexus clear
        if (args.length == 1) {
            manager.clearAll();
            sender.sendMessage(ChatColor.GOLD + "All Nexuses have been cleared!");
            return true;
        }

        // On command  /nexus clear <team>
        String team = args[1];
        if (manager.clearTeam(team)) {
            plugin.getTabUpdater().updateNow();
            sender.sendMessage("§cThe §6" + team + " §cteam Nexus has been destroyed.");
        } else {
            sender.sendMessage("§cThe §e" + team + " §cteam doesn't have a registered Nexus.");
        }
        return true;
    }

    private boolean handleToggle(CommandSender sender, boolean turnOn) {

        if (!sender.hasPermission("nexusteams.admin")) {
            sender.sendMessage("§cYou don't have permissions to execute this command.");
            return true;
        }
        boolean already = plugin.isActive();
        if (already == turnOn) {
            sender.sendMessage(turnOn
                    ? "§eNexusCoreTeams plugin is already enabled."
                    : "§eNexusCoreTeams plugin is already disabled.");
            return true;
        }

        plugin.setActive(turnOn);
        
        plugin.getConfig().set("enabled", turnOn);
        plugin.saveConfig();

        if (turnOn) {
            plugin.getTabUpdater().start();
            sender.sendMessage("§aNexusCoreTeams plugin is enabled.");
        } else {
            handleClear(sender, new String[] {"clear"});
            plugin.getTabUpdater().stop();
            sender.sendMessage("§cNexusCoreTeams plugin is disabled.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        // first argument
        if (args.length == 1) {
            return Stream.of("set","hp","info","clear","enable","disable", "reload")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String sub = args[0].toLowerCase();
        // second argument
        if (args.length == 2 && List.of("set","hp","info","clear").contains(sub)) {
            // team list from Scoreboard
            return plugin.getServer().getScoreboardManager()
                    .getMainScoreboard().getTeams().stream()
                    .map(Team::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // third argument
        if (sub.equals("set") && args.length == 3) {
            return List.of(String.valueOf(plugin.getConfig().getInt("default-nexus-hp", 30)));
        }

        // for /nexus hp <team> <value>
        if (sub.equals("hp") && args.length == 3) {
            String team = args[1];
            TeamData td = manager.getTeamData(team);
            if (td != null) {
                int max = td.getMaxHealth();
                return Stream.of(
                                "1",
                                String.valueOf(max/2),
                                String.valueOf(max)
                        )
                        .filter(s -> s.startsWith(args[2]))
                        .collect(Collectors.toList());
            }
        }

        // /nexus clear <team>
        if (sub.equals("clear") && args.length == 2) {
            List<String> teams = manager.getTeamDataMap().keySet().stream().toList();
            return teams.stream()
                    .filter(t -> t.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // no hints
        return Collections.emptyList();
    }
}
