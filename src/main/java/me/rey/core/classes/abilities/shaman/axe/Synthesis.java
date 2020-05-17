package me.rey.core.classes.abilities.shaman.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilParticle;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Synthesis extends Ability {
    public Synthesis() {
        super(511, "Synthesis", ClassType.GREEN, AbilityType.AXE, 1, 3, 12, Arrays.asList(
                "Summon the power of the sun, giving",
                "allies within <variable>4.5+0.5*l</variable> blocks Strength for",
                "<variable>2.5+0.5*l</variable> seconds",
                "",
                "Energy Consumption: <variable>60</variable>",
                "",
                "Recharge: <variable>12</variable> seconds"
        ));
        this.setEnergyCost(60);
    }

    public HashMap<UUID, Integer> synthTicks = new HashMap<UUID, Integer>();

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        double radius = 4.5+0.5*level;
        int duration = (int) (2.5+0.5*level)*20;
        Location loc = p.getLocation().clone();

        loc.getWorld().playSound(loc, Sound.CAT_HISS, 1F, 1.5F);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(synthTicks.containsKey(p.getUniqueId()) == false) {
                    synthTicks.put(p.getUniqueId(), 0);
                    for (Entity e : UtilBlock.getEntitiesInCircle(p.getLocation(), radius)) {
                        if (e instanceof Player) {
                            Player p = (Player) e;
                            if(u.getTeam().contains(p)) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, 0));
                            }
                        }
                    }
                } else {

                    if(synthTicks.get(p.getUniqueId()) > 5) {
                        synthTicks.remove(p.getUniqueId());
                        this.cancel();
                        return;
                    } else {
                        synthTicks.replace(p.getUniqueId(), synthTicks.get(p.getUniqueId())+1);
                    }

                    for(Location loc : UtilBlock.circleLocations(loc, radius*synthTicks.get(p.getUniqueId())/5)) {
                        UtilParticle.playColoredParticle(loc, 250F, 250F, 120F);
                    }

                }
            }
        }.runTaskTimer(Warriors.getInstance(), 0L, 1L);

        return true;
    }
}
