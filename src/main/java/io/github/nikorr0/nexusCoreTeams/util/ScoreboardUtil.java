package io.github.nikorr0.nexusCoreTeams.util;

import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Method;

public final class ScoreboardUtil {

    private static Method ADD_ENTITY;

    public static void addEntity(Team team, Entity ent) {
        try {
            if (ADD_ENTITY == null)
                ADD_ENTITY = Team.class.getMethod("addEntity", Entity.class);

            ADD_ENTITY.invoke(team, ent);
        } catch (NoSuchMethodException ignore) {
            team.addEntry(ent.getUniqueId().toString());
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }
}