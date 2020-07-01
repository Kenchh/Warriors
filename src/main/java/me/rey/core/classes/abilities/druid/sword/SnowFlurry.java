package me.rey.core.classes.abilities.druid.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.gui.Gui;
import me.rey.core.items.Throwable;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Array;
import java.util.*;

public class SnowFlurry extends Ability implements IConstant {

    final double baseAmount = 0;
    final double amountPerLevel = 1;

    public SnowFlurry() {
        super(203, "Snow Flurry", ClassType.GOLD, AbilityType.SWORD, 1, 3, 0.0, Arrays.asList(
                "Fire a pile of <variable>1*l</variable> (+1) snowballs, pushing hit",
                "enemies into the air.",
                "",
                "Energy: <variable>40-l*2</variable> (-2) per second."
        ));
        this.setIgnoresCooldown(true);
        this.setEnergyCost(40/20, 2/20);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        this.setEnergyCost(0, 0);

        if(!p.isBlocking()) {
            return false;
        }

        this.setEnergyCost(40/20, 2/20);

        for(int i=1; i<=baseAmount+amountPerLevel*level; i++)
            new FlurryObject(p, u, level);

        return true;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if(e.getDamager().getType() == EntityType.SNOWBALL && e.getDamager().getCustomName().contains("§4FL")) {
            e.setCancelled(true);
        }
    }

    class FlurryObject {

        Player p;
        User u;
        int level;

        Snowball flurry;

        final double maxspreadH = 0.25;
        final double maxspreadY = 0.25;

        public FlurryObject(Player p, User u, int level) {
            this.p = p;
            this.u = u;
            this.level = level;

            Location origin = p.getLocation();

            Location loc = p.getEyeLocation();
            double yMultiplier = Math.sqrt(1-Math.pow(UtilBlock.getYCordsMultiplierByPitch(p.getLocation().getPitch()), 2));
            loc.setX(UtilBlock.getXZCordsFromDegree(p.getLocation(), yMultiplier, p.getLocation().getYaw() + 90)[0]);
            loc.setY(loc.getY()+UtilBlock.getYCordsMultiplierByPitch(p.getLocation().getPitch())-0.5);
            loc.setZ(UtilBlock.getXZCordsFromDegree(p.getLocation(), yMultiplier, p.getLocation().getYaw() + 90)[1]);

            double xMultiplier = UtilBlock.getXZCordsMultipliersFromDegree(p.getLocation().getYaw() + 90 + 45)[0];
            double zMultiplier = UtilBlock.getXZCordsMultipliersFromDegree(p.getLocation().getYaw() + 90 + 45)[1];

            Random sx = new Random();
            double xspread = (double) sx.nextInt((int) (maxspreadH*1000)) / 1000;

            Random sz = new Random();
            double zspread = (double) sz.nextInt((int) (maxspreadH*1000)) / 1000;

            Random pnH = new Random();
            boolean positiveH = pnH.nextBoolean();

            if(!positiveH) {
                xspread = -xspread;
                zspread = -zspread;
            }

            Random sy = new Random();
            double yspread = (double) sy.nextInt((int) (maxspreadY*1000)) / 1000;

            loc.setX(loc.getX() + xspread*xMultiplier);
            loc.setY(loc.getY() + yspread);
            loc.setZ(loc.getZ() + zspread*zMultiplier);

            flurry = (Snowball) p.getWorld().spawnEntity(loc, EntityType.SNOWBALL);
            flurry.setCustomName("§4FL");

            /* Vertical random angle spread */

            Random pnyr = new Random();

            Random rdy = new Random();
            double randomPitchDegree = rdy.nextInt(10); /* Minimum curve 10, max 25 */

            if(!pnyr.nextBoolean()) {
                randomPitchDegree = -randomPitchDegree;
            }

            origin.setPitch(origin.getPitch() + (float) randomPitchDegree);

            /* Vertical random angle spread */

            /* Horizontal random angle spread */

            Random pnhr = new Random();

            Random rdh = new Random();
            double randomYawDegree = rdh.nextInt(10); /* Minimum curve 10, max 25 */

            if(!pnhr.nextBoolean()) {
                randomYawDegree = -randomYawDegree;
            }

            origin.setYaw(origin.getYaw() + (float) randomYawDegree);

            /* Horizontal random angle spread */

            Vector direction = origin.getDirection();
            flurry.setVelocity(direction.normalize().multiply(1));

            flurry.getWorld().playSound(flurry.getLocation(), Sound.HORSE_BREATHE, 1F, 1.25F);

            new BukkitRunnable() {
                @Override
                public void run() {

                    for(Entity e : flurry.getWorld().getNearbyEntities(flurry.getLocation(), 0.5, 1.5, 0.5)) {
                        if(e instanceof LivingEntity) {
                            if(e != p) {
                                e.setVelocity(flurry.getVelocity().normalize().multiply(0.15).setY(0.15));
                                e.setFallDistance(0);
                            }
                        }
                    }

                    if(origin.distance(flurry.getLocation()) >= 15 || flurry.isDead()) {
                        flurry.remove();
                        this.cancel();
                        return;
                    }
                }
            }.runTaskTimer(Warriors.getInstance(), 1L, 1L);

        }

    }

}
