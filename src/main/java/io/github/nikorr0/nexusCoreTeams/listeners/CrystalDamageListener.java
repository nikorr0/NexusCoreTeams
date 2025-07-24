package io.github.nikorr0.nexusCoreTeams.listeners;

import io.github.nikorr0.nexusCoreTeams.NexusManager;
import io.github.nikorr0.nexusCoreTeams.TeamData;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class CrystalDamageListener implements Listener {

    private static NexusManager manager;


    public CrystalDamageListener(NexusManager manager) {
        CrystalDamageListener.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onExplosionDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof EnderCrystal)) return;

        EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            e.setCancelled(true); // cancel damage for end crystal
        }
    }

    /*
        Melee damage - swords, arrows.
        Triggered ONLY if the event has not been canceled yet (not an explosion)
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDirectHit(EntityDamageByEntityEvent e) {
        if (!manager.getPlugin().isActive()) return;
        if (!(e.getEntity() instanceof EnderCrystal crystal)) return;

        EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)
            return;

        TeamData td = manager.getTeamDataByCrystal(crystal.getUniqueId());
        if (td == null) return;

        // Counting how much time has passed since the last strike
        double now = System.currentTimeMillis();
        double since = now - td.getLastHitMs();
        double cooldown = manager.getPlugin().config().getHitCooldownSeconds();

        if (since < cooldown) {
            double remainingMs = cooldown - since;
            double remSec = (double) Math.round(remainingMs / 10) / 100;

            String remSecStr = "" + remSec;
            if (remSecStr.split("\\.")[1].length() == 1) {
                remSecStr += "0";
            }

            // If it's still in the cooldown, we'll notify the attacker in the ActionBar.
            if (e.getDamager() instanceof Player p) {
                p.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        new TextComponent("§7The cooldown will expire in "
                                + remSecStr + " seconds")
                );
            }
            e.setCancelled(true);
            return;
        }

        if (td.getCrystalUuid().equals(crystal.getUniqueId())) {
            int damage;
            e.setCancelled(true); // blocking vanilla explosion/breaking

            if (e.getDamager() instanceof Player player) {
                if (manager.getPlugin().config().getWeaponDamageEnabled()) {
                    Double damageDouble = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
                    damage = damageDouble == null ? 1 : Math.toIntExact(Math.round(damageDouble));
                }
                else {
                    damage = 1;
                }
            }

            else if (manager.getPlugin().config().getArrowDamageEnabled() &&
                    e.getDamager() instanceof AbstractArrow arrow) {
                Double damageDouble = arrow.getDamage();
                arrow.remove();
                damage = damageDouble == null ? 1 : Math.toIntExact(Math.round(damageDouble));
            }

            else {
                return;
            }

            manager.damageNexus(td.getTeamName(), damage);

            // if HP <= 0 - we break the crystal
            // with explosion and sound
            if (!td.isAlive()) {
                crystal.getWorld().playSound(crystal.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);
                crystal.getWorld().createExplosion(crystal.getLocation(), 2, false);
                crystal.remove();
            }
            return;
        }
        e.setCancelled(true);
    }
}
