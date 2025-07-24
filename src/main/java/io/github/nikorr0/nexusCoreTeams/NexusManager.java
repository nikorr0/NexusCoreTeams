package io.github.nikorr0.nexusCoreTeams;

import io.github.nikorr0.nexusCoreTeams.util.ScoreboardUtil;
import io.github.nikorr0.nexusCoreTeams.util.TeamColors;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NexusManager {

    private final NexusCoreTeams plugin;
    private final Map<String, TeamData> teams = new HashMap<>();
    private final File dataFile;
    private final FileConfiguration dataCfg;
    private final NamespacedKey TAG_NEXUS;

    public NexusManager(NexusCoreTeams plugin) {
        this.plugin = plugin;
        this.TAG_NEXUS = new NamespacedKey(plugin, "nexus_team");

        dataFile = new File(plugin.getDataFolder(), "nexus.yml");
        dataCfg = YamlConfiguration.loadConfiguration(dataFile);
        load();
    }

    /**
      Removes all Ender Crystals located within a 5x5 square
      centred on the stored Nexus position (+- 2 blocks on X/Z, +- 3 blocks on Y).
      Intended for safe clean-up after shutdown or /reload:
      every crystal that could visually overlap the Nexus pedestal is purged.
     */
    public void despawnCrystals() {

        for (TeamData td : teams.values()) {

            Location nexusLoc = td.getNexusLocation();
            if (nexusLoc == null) continue;

            World world = nexusLoc.getWorld();

            // Loading the chunk so that crystal is guaranteed to appear in the pool
            int cx = nexusLoc.getBlockX() >> 4;
            int cz = nexusLoc.getBlockZ() >> 4;
            if (!world.isChunkLoaded(cx, cz)) {
                world.loadChunk(cx, cz, false);
            }

            double radius = 2.0;   // horizontal +- 2 block
            double yRange = 3.0;   // vertical +- 3 block

            world.getNearbyEntities(nexusLoc, radius, yRange, radius).stream()
                    .filter(ent -> ent instanceof EnderCrystal)
                    .forEach(Entity::remove);
        }
    }

    public TeamData getTeamData(String team) { return teams.get(team); }

    public boolean isTeamAlive(String team) {
        TeamData td = teams.get(team);
        return td != null && td.isAlive();
    }

    public TeamData getTeamDataByCrystal(UUID crystalUuid) {
        for (TeamData td : teams.values()) {
            if (crystalUuid.equals(td.getCrystalUuid())) {
                return td;
            }
        }
        return null;
    }

    public void damageNexus(String team, int dmg) {
        if (!plugin.isActive()) return;

        TeamData td = teams.get(team);
        if (td == null || !td.isAlive()) return;

        long now = System.currentTimeMillis();
        if (now - td.getLastHitMs() < plugin.config().getHitCooldownSeconds()) {
            return; // cooldown is active — we ignore the strike
        }
        td.setLastHitMs(now);

        int prevHp   = td.getHealth();
        int newHp    = Math.max(prevHp - dmg, 0);
        int maxHp    = td.getMaxHealth();

        td.setHealth(newHp);

        int beforePct = prevHp * 100 / maxHp;
        int afterPct  = newHp  * 100 / maxHp;

        // Run through the threshold levels: 100–notifyPct, 100–2*notifyPct, ... > 0
        int notifyPercent = plugin.config().getNotifyPercent();
        for (int i = 1; i * notifyPercent < 100; i++) {
            int threshold = 100 - i * notifyPercent;
            if (beforePct > threshold && afterPct <= threshold) {
                // Send them only to the players of the team
                String msg = "§eYour team's Nexus has lost "
                        + notifyPercent + "% of its HP! §7("
                        + newHp + " HP left)";

                var sbTeam = Bukkit.getScoreboardManager()
                        .getMainScoreboard()
                        .getTeam(team);
                if (sbTeam != null) {
                    for (OfflinePlayer op : sbTeam.getPlayers()) {
                        Player p = op.getPlayer();
                        if (p != null && p.isOnline()) {
                            p.sendMessage(msg);
                        }
                    }
                }
                break;  // Notify you only once per hit.
            }
        }

        if (td.getHealth() <= 0) {
            td.setAlive(false);

            ChatColor cc  = TeamColors.get(team);

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage("§cThe " + cc + team + " §cteam's Nexus has been destroyed!");
            }

        } else {
            Location particleLoc = td.getNexusLocation().clone().add(0.5, 1, 0.5);
            particleLoc.getWorld().spawnParticle(
                    Particle.CRIT_MAGIC,
                    particleLoc,
                    10, 0.3, 0.3, 0.3, 0.02);
        }
        plugin.getTabUpdater().updateNow();
    }

    public void registerNexus(String team, Location loc, int hp) {
        if (!plugin.isActive()) return;
        // Spawning end crystal
        Location spawnLoc = loc.clone().add(0.5, 1, 0.5);
        EnderCrystal crystal = loc.getWorld().spawn(spawnLoc.clone(), EnderCrystal.class, e -> {
            e.setShowingBottom(false);
            e.setInvulnerable(false);
        });

        ChatColor cc  = TeamColors.get(team);

        crystal.setCustomName(team + " Nexus");
        crystal.setCustomNameVisible(true);
        crystal.setGlowing(true);

        // creating/taking a service scoreboard command so that the outline is the same color
        org.bukkit.scoreboard.Team glow =
                Bukkit.getScoreboardManager().getMainScoreboard()
                        .getTeam("NEXUS_" + team);

        if (glow == null)
            glow = Bukkit.getScoreboardManager().getMainScoreboard()
                    .registerNewTeam("NEXUS_" + team);

        glow.setColor(cc);
        ScoreboardUtil.addEntity(glow, crystal);

        // saving data
        TeamData td = new TeamData(team, spawnLoc.clone(), hp, true, crystal.getUniqueId(), hp);
        teams.put(team, td);
        saveAll();
    }

    // Running when restarting the server
    private void load() {
        if (!plugin.isActive() || !dataFile.exists()) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            despawnCrystals();

            for (String team : dataCfg.getKeys(false)) {
                World world = Bukkit.getWorld(dataCfg.getString(team + ".world"));
                if (world == null) continue;

                Location base = new Location(
                        world,
                        dataCfg.getDouble(team + ".x"),
                        dataCfg.getDouble(team + ".y"),
                        dataCfg.getDouble(team + ".z"));
                int hp = dataCfg.getInt(team + ".hp");
                boolean alive = dataCfg.getBoolean(team + ".alive", true);
                UUID crystalUUID = dataCfg.contains(team + ".uuid") ? UUID.fromString(dataCfg.getString(team + ".uuid")) : null;

                EnderCrystal crystal = null;

                // If there is a UUID in nexus.yml, search for crystal using it
                if (crystalUUID != null) {
                    Entity ent = Bukkit.getEntity(crystalUUID);
                    if (ent instanceof EnderCrystal) {
                        crystal = (EnderCrystal) ent;
                    }
                }

                // If it haven't found crystal by UUID, create a new one
                if (alive) {
                    if (crystal == null) {
                        Location spawnLoc = base.clone();
                        // Spawning new crystal
                        crystal = spawnLoc.getWorld().spawn(
                                base.clone(),
                                EnderCrystal.class,
                                e -> {
                                    e.setShowingBottom(false);
                                    e.setInvulnerable(false);
                                }
                        );
                        crystalUUID = crystal.getUniqueId(); // updating the UUID for future reference
                    }

                    // Setting up the crystal (name, color, glow)
                    crystal.getPersistentDataContainer()
                            .set(TAG_NEXUS, PersistentDataType.STRING, team);

                    ChatColor glowCol = TeamColors.get(team);

                    crystal.setCustomName(glowCol + team + " Nexus");
                    crystal.setCustomNameVisible(true);
                    crystal.setGlowing(true);

                    org.bukkit.scoreboard.Team glow = Bukkit.getScoreboardManager()
                            .getMainScoreboard()
                            .getTeam("NEXUS_" + team);
                    if (glow == null)
                        glow = Bukkit.getScoreboardManager()
                                .getMainScoreboard()
                                .registerNewTeam("NEXUS_" + team);
                    glow.setColor(glowCol);
                    ScoreboardUtil.addEntity(glow, crystal);

                    // updating TeamData
                    TeamData td = new TeamData(team, base.clone(), hp, alive, crystalUUID, hp);
                    teams.put(team, td);
                }
            }
            saveAll();
        }, 150L);
    }

    public void saveAll() {
        for (Map.Entry<String, TeamData> e : teams.entrySet()) {
            String t  = e.getKey();
            TeamData d = e.getValue();
            dataCfg.set(t + ".uuid", d.getCrystalUuid().toString());
            dataCfg.set(t + ".world", d.getNexusLocation().getWorld().getName());
            dataCfg.set(t + ".x", d.getNexusLocation().getX());
            dataCfg.set(t + ".y", d.getNexusLocation().getY());
            dataCfg.set(t + ".z", d.getNexusLocation().getZ());
            dataCfg.set(t + ".hp", d.getHealth());
            dataCfg.set(t + ".alive", d.isAlive());
        }
        try { dataCfg.save(dataFile); } catch (IOException ex) { ex.printStackTrace(); }
    }

    public void clearAll() {
        // Removing crystals from the world
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();

        // Removing the crystal and scoreboard for each team
        for (TeamData td : teams.values()) {
            // deleting crystal
            UUID id = td.getCrystalUuid();
            if (id != null) {
                Entity e = Bukkit.getEntity(id);
                if (e instanceof EnderCrystal) e.remove();
            }
            // deleting Scoreboard-team
            String glowName = "NEXUS_" + td.getTeamName();
            org.bukkit.scoreboard.Team glow = sb.getTeam(glowName);
            if (glow != null) glow.unregister();
        }

        // Clearing memory and file
        teams.clear();
        for (String key : dataCfg.getKeys(false)) {
            dataCfg.set(key, null);
        }
        saveAll();
    }

    public boolean clearTeam(String team) {
        TeamData td = teams.remove(team);
        if (td == null) return false;

        // deleting crystal
        UUID id = td.getCrystalUuid();
        if (id != null) {
            Entity e = Bukkit.getEntity(id);
            if (e instanceof EnderCrystal) e.remove();
        }

        // deleting Scoreboard-team
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Team glow = sb.getTeam("NEXUS_" + team);
        if (glow != null) glow.unregister();

        // clearing section in file
        dataCfg.set(team, null);
        saveAll();
        return true;
    }

    public boolean hasNexus(String team) {
        return teams.containsKey(team);   // has entry in memory
    }

    public NexusCoreTeams getPlugin() {
        return plugin;
    }

    public Map<String, TeamData> getTeamDataMap() { return teams; }
}
