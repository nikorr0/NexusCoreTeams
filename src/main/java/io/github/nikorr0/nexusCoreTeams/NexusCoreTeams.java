package io.github.nikorr0.nexusCoreTeams;

import io.github.nikorr0.nexusCoreTeams.commands.NexusCommand;
import io.github.nikorr0.nexusCoreTeams.listeners.CrystalDamageListener;
import io.github.nikorr0.nexusCoreTeams.listeners.PlayerRespawnListener;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class NexusCoreTeams extends JavaPlugin {
    private boolean active;
    private NexusManager nexusManager;
    private TabUpdater  tabUpdater;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        active = getConfig().getBoolean("enabled", true);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        nexusManager = new NexusManager(this);

        // listeners
        getServer().getPluginManager().registerEvents(
                new CrystalDamageListener(nexusManager), this);
        getServer().getPluginManager().registerEvents(
                new PlayerRespawnListener(nexusManager), this);

        tabUpdater = new TabUpdater(this, nexusManager);
        tabUpdater.start();

        NexusCommand cmd = new NexusCommand(this.getNexusManager(), nexusManager.getPlugin());
        getCommand("nexus").setExecutor(cmd);
        getCommand("nexus").setTabCompleter(cmd);

        if (!active) {
            getLogger().warning("NexusCoreTeams disabled via config.yml");
            tabUpdater.stop();
            return;
        }

        getLogger().info("NexusCoreTeams plugin enabled");
    }

    @Override
    public void onDisable() {
        nexusManager.despawnCrystals();
        nexusManager.saveAll();
        if (tabUpdater != null) tabUpdater.stop();
        getLogger().info("NexusCoreTeams plugin disabled");
    }

    public void reloadPluginConfig(CommandSender trigger) {
        reloadConfig();
        nexusManager.refreshConfig();

        trigger.sendMessage("§aNexusCoreTeams: the config has been reloaded.");
        getLogger().info(trigger.getName() + " reloaded config.");
    }

    public NexusManager getNexusManager() {
        return nexusManager;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean b) { active = b; }
    public TabUpdater getTabUpdater() { return tabUpdater; }
}
