package me.rey.core.classes.abilities.ninja;

import com.avaje.ebean.Update;
import jdk.nashorn.internal.ir.Block;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.events.customevents.AbilityFailEvent;
import me.rey.core.events.customevents.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.pvp.ToolType;
import me.rey.core.utils.BlockLocation;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.*;

public class Evade extends Ability {

    public HashMap<UUID, Integer> preparingEvade = new HashMap<UUID, Integer>();
    public HashMap<UUID, Integer> invincible = new HashMap<UUID, Integer>();

    public Evade() {
        super(10, "Evade", ClassType.LEATHER, AbilityType.SWORD, 2, 1, 8, Arrays.asList(
                "Teleports you behind your attacker, and gives",
                "invincibility frames for a very short amount of time.",
                "Frames are canceled immediately on an attack.",
                "1.5 second cooldown on a successful evade.",
                "8 second cooldown on an unsuccessful evade.",
                "2 seconds while holding sword fails evade."
        ));
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(new User(p).isUsingAbility(this) == false) return;

            if(invincible.containsKey(p.getUniqueId())) {
                if(invincible.get(p.getUniqueId()) >= 20) {
                    invincible.remove(p.getUniqueId());
                } else {
                    invincible.replace(p.getUniqueId(), invincible.get(p.getUniqueId()) + 1);
                }
            }

            if(preparingEvade.containsKey(p.getUniqueId())) {
                if(preparingEvade.get(p.getUniqueId()) >= 20 || p.isBlocking() == false) {
                    preparingEvade.remove(p.getUniqueId());

                    setIgnoresCooldown(false);
                    applyCooldown(p);
                    sendAbilityMessage(p, ChatColor.RED + "Failed to evade.");

                } else {
                    preparingEvade.replace(p.getUniqueId(), preparingEvade.get(p.getUniqueId()) + 1);
                }
            }
        }
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        if(preparingEvade.containsKey(p.getUniqueId()) == false) {
            preparingEvade.put(p.getUniqueId(), 0);
        }

        setIgnoresCooldown(true);

        return true;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof Player == false) {
            return;
        }

        if(e.getDamager() instanceof Player == false) {
            return;
        }

        Player damager = (Player) e.getDamager();
        Player damagee = (Player) e.getEntity();

        if(new User(damagee).isUsingAbility(this) == false) {
            return;
        }

        if(invincible.containsKey(damagee.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        if (preparingEvade.containsKey(damagee.getUniqueId()) == false) {
            return;
        }

        e.setCancelled(true);

        preparingEvade.remove(damagee.getUniqueId());

        invincible.put(damagee.getUniqueId(), 0);

        this.setCooldown(1);
        this.applyCooldown(damagee);

        for(int i=0; i<=8; i++) {
            damagee.getWorld().playSound(damagee.getLocation(), Sound.ENDERDRAGON_WINGS, 1.5f, 0.5f);
        }
        damagee.getWorld().spigot().playEffect(damagee.getLocation(), Effect.LARGE_SMOKE, 0, 0, 0F, 0F, 0F, 0F, 10, 100);

        tpBehindPlayer(damagee, damager);


    }

    private void tpBehindPlayer(Player damagee, Player damager) {
        Location tpLoc = damager.getLocation();
        tpLoc.add(tpLoc.getDirection().multiply(-2));

        Location locInBetween = damager.getLocation();
        locInBetween.add(locInBetween.getDirection().multiply(-1));

        tpLoc.setY(damager.getLocation().getY());
        locInBetween.setY(damager.getLocation().getY());

        if(damager.getLocation().getBlock().getType().isSolid() == false && BlockLocation.getBlockAbove(damager.getLocation().getBlock()).getType().isSolid() == false) {
            if (locInBetween.getBlock().getType().isSolid() == false && BlockLocation.getBlockAbove(locInBetween.getBlock()).getType().isSolid() == false) {

                if (tpLoc.getBlock().getType().isSolid() == false && BlockLocation.getBlockAbove(tpLoc.getBlock()).getType().isSolid() == false) {
                    damagee.teleport(tpLoc);
                    return;
                }

                damagee.teleport(locInBetween);
                return;
            }
        }

        damagee.teleport(damager.getLocation());
    }

}
