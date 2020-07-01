package me.rey.core.classes.abilities.shaman.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.gui.Gui;
import me.rey.core.items.Throwable;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilParticle;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.LongGrass;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Overgrown extends Ability {
    public Overgrown() {
        super(512, "Overgrown", ClassType.GREEN, AbilityType.AXE, 1, 3, 10, Arrays.asList(
                "Right clicking grants you Absorption I, while allies receive",
                "Absorption II in a radius of <variable>4.5+0.5*l</variable> blocks.",
                "for 5 seconds.",
                "",
                "Energy: <variable>94-4*l</variable>",
                "",
                "Recharge: <variable>10.5-0.5*l</variable>"
        ));
        this.setEnergyCost(10.5, 0.5);
    }

    public HashMap<UUID, Integer> ticks = new HashMap<UUID, Integer>();

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {
        this.setCooldown(10.5-0.5*level);

        double radius=4.5+0.5*level;

        for(Entity e : UtilBlock.getEntitiesInCircle(p.getLocation(), radius)) {
            if(e instanceof Player) {
                Player ps = (Player) e;

                if(ps == p) {
                    ps.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*5, 0));
                    continue;
                }

                if(u.getTeam().contains(ps)) {
                    ps.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*5, 1));
                }
            }
        }

        ticks.put(p.getUniqueId(), 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(ticks.get(p.getUniqueId()) <= 10) {
                    ticks.replace(p.getUniqueId(), ticks.get(p.getUniqueId())+1);
                } else {
                    ticks.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }

                p.getWorld().playSound(p.getLocation(), Sound.CREEPER_HISS, 1F, 0.3F);

                for(int degree=0; degree<=360; degree += 40) {

                    Location loc = p.getLocation().clone();

                    double x = UtilBlock.getXZCordsFromDegree(loc, radius, degree)[0];
                    double z = UtilBlock.getXZCordsFromDegree(loc, radius, degree)[1];

                    loc.setX(x);
                    loc.setZ(z);

                    UtilParticle.playColoredParticle(loc, 100, 255, 180);

                    if(((double) degree + ticks.get(p.getUniqueId())*10) % 45 == 0) {
                        for(double i=0; i<=radius; i+=0.25) {
                            Location lineloc = p.getLocation().clone();

                            double x2 = UtilBlock.getXZCordsFromDegree(lineloc, i, degree)[0];
                            double z2 = UtilBlock.getXZCordsFromDegree(lineloc, i, degree)[1];

                            lineloc.setX(x2);
                            lineloc.setZ(z2);

                            UtilParticle.playColoredParticle(lineloc, 100, 255, 180);

                            Block b = UtilBlock.highestLocation(lineloc).getBlock();

                            if(b.getType() == Material.AIR &&
                                    (UtilBlock.getBlockUnderneath(b).getType() == Material.GRASS || UtilBlock.getBlockUnderneath(b).getType() == Material.DIRT)) {
                                b.setType(Material.LONG_GRASS);
                                b.setData((byte) 1);

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if(b.getType() == Material.LONG_GRASS) {
                                            b.setType(Material.AIR);
                                        }
                                    }
                                }.runTaskLater(Warriors.getInstance(), 40L);

                            }

                        }

                    }

                }

            }
        }.runTaskTimer(Warriors.getInstance(), 0L ,1L);



        return true;
    }
}
