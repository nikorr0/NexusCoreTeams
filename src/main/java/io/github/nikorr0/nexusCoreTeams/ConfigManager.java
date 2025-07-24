package io.github.nikorr0.nexusCoreTeams;

import org.bukkit.configuration.file.FileConfiguration;

import static org.bukkit.Bukkit.getLogger;

public final class ConfigManager {

    private boolean pluginEnabled;
    private int defaultNexusHp;
    private int notifyPercent;
    private double hitCooldownSeconds;
    private boolean arrowDamageEnabled;
    private boolean weaponDamageEnabled;

    private final NexusCoreTeams plugin;

    public ConfigManager(NexusCoreTeams plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        pluginEnabled = cfg.getBoolean("enabled", true);
        defaultNexusHp = cfg.getInt("default-nexus-hp", 30);

        if (defaultNexusHp <= 0) {
            getLogger().info("Wrong value of <default-nexus-hp> parameter. Setting <default-nexus-hp> to 1.");
            defaultNexusHp = 1;
        }

        notifyPercent = cfg.getInt("notify-percent", 20);

        if (notifyPercent <= 0) {
            getLogger().info("Wrong value of <notify-percent> parameter. Setting <notify-percent> to 100.");
            notifyPercent = 100;
        }

        hitCooldownSeconds = cfg.getDouble("hit-cooldown", 0.5) * 1000L;

        if (hitCooldownSeconds < 0) {
            getLogger().info("Wrong value of <hit-cooldown> parameter. Setting <hit-cooldown> to 0.");
            hitCooldownSeconds = 0;
        }

        weaponDamageEnabled = cfg.getBoolean("weapon-damage-enabled", true);
        arrowDamageEnabled = cfg.getBoolean("arrow-damage-enabled", true);
    }

    public boolean isPluginEnabled() { return pluginEnabled; }
    public int getDefaultNexusHp() { return defaultNexusHp; }
    public int getNotifyPercent() { return notifyPercent; }
    public double getHitCooldownSeconds() { return hitCooldownSeconds; }
    public boolean getWeaponDamageEnabled() { return weaponDamageEnabled; }
    public boolean getArrowDamageEnabled() { return arrowDamageEnabled; }
}

