package io.github.nikorr0.nexusCoreTeams;

import org.bukkit.Location;

import java.util.UUID;

public class TeamData {

    private final String teamName;
    private final Location nexusLocation;
    private final int maxHealth;
    private int health;
    private boolean alive;
    private UUID crystalUuid;
    private double lastHitMs = 0L;

    public TeamData(String teamName, Location nexusLocation,
                    int health, boolean alive,
                    UUID crystalUuid, int maxHealth) {
        this.teamName     = teamName;
        this.nexusLocation= nexusLocation;
        this.health       = health;
        this.alive        = alive;
        this.crystalUuid  = crystalUuid;
        this.maxHealth    = maxHealth;
    }

    public TeamData(String teamName, Location nexusLocation, int health) {
        this(teamName, nexusLocation, health, true, null, health);
    }

    public String getTeamName() { return teamName; }
    public Location getNexusLocation() { return nexusLocation; }
    public UUID getCrystalUuid() { return crystalUuid; }
    public void setCrystalUuid(UUID crystalUuid) { this.crystalUuid = crystalUuid; }
    public int getMaxHealth() { return maxHealth; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    public double getLastHitMs() { return lastHitMs; }
    public void setLastHitMs(double timeMs) { this.lastHitMs = timeMs; }
}
