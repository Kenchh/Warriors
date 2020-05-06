package me.rey.core.classes.abilities.ninja;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BladeVortex extends Ability {

    private final double radius = 4;

    public BladeVortex() {
        super(4, "Blade Vortex", ClassType.LEATHER, AbilityType.SWORD, 1, 3, 6.5, Arrays.asList(
                "Create a blade vortex, pulling players into you",
                "and casting players near you afar.",
                "",
                "Players hit with the blade vortex take <variable>5+l</variable> damage",
                "as well as receive slowness for <variable>1.0 + 0.5*l</variable> seconds.",
                "",
                "Recharge: <variable>7.5 - l</variable>"
        ));
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        this.setCooldown(10.5 - level);

        for(Entity e : p.getNearbyEntities(6, 6, 6)) {
            double distance = p.getLocation().distance(e.getLocation());
            if(inCircle(p, e)) {
                if (distance < 2.6) {
                    pushAway(p, e);
                } else {
                    pushIn(p, e);
                }
            }
        }

        return false;
    }

    public boolean inCircle(Player p, Entity e) {

        HashMap<Double, double[]> maxmincords = new HashMap<Double, double[]>();

        for(double degree=0; degree<=90; degree++) {

            double radian = Math.toRadians(degree);

            double maxXmultiplier = Math.cos(radian);
            double maxZmultiplier = Math.sin(radian);

            double maxX = maxXmultiplier * radius;
            double maxZ = maxZmultiplier * radius;

            double[] maxCords = new double[2];

            double maxXCords = p.getLocation().getX() + maxX;
            double maxZCords = p.getLocation().getZ() + maxZ;

            maxCords[0] = maxXCords;
            maxCords[1] = maxZCords;

            maxmincords.put(degree, maxCords);

        }

        for(double degree=180; degree<=270; degree++) {

            double radian = Math.toRadians(degree);

            double minXmultiplier = Math.cos(radian);
            double minZmultiplier = Math.sin(radian);

            double minX = minXmultiplier * radius;
            double minZ = minZmultiplier * radius;

            double[] minCords = new double[2];

            double minXCords = p.getLocation().getX() + minX;
            double minZCords = p.getLocation().getZ() + minZ;

            minCords[0] = minXCords;
            minCords[1] = minZCords;

            maxmincords.put(degree, minCords);

        }

        for(double degree : maxmincords.keySet()) {
            double[] cords = maxmincords.get(degree);

            double xCords = cords[0];
            double zCords = cords[1];

            Location loc = new Location(p.getWorld(), xCords, p.getLocation().getY() + 1, zCords);

            p.getWorld().spigot().playEffect(loc, Effect.FIREWORKS_SPARK, 0, 0, 0F, 0F, 0F, 0F, 5, 50);

        }

        for(double degree=0;degree<=90;degree++) {

            double[] maxcords = maxmincords.get(degree);
            double[] mincords = maxmincords.get(180 + degree);

            double maxX = maxcords[0];
            double maxZ = maxcords[1];

            double minX = mincords[0];
            double minZ = mincords[1];

            if(p.getLocation().getX() <= maxX && p.getLocation().getZ() <= maxZ && p.getLocation().getX() >= minX && p.getLocation().getZ() >= minZ) {
                return true;
            } else {
                continue;
            }

        }



        return false;
    }

    public void pushAway(Player user, Entity pToPush) {
        double pX = user.getLocation().getX();
        double pY = user.getLocation().getY();
        double pZ = user.getLocation().getZ();

        double tX = pToPush.getLocation().getX();
        double tY = pToPush.getLocation().getY();
        double tZ = pToPush.getLocation().getZ();

        double deltaX = tX - pX;
        double deltaY = tY - pY;
        double deltaZ = tZ - pZ;

        pToPush.setVelocity(new Vector(deltaX, deltaY, deltaZ).normalize().multiply(1.25D).setY(0.3D));
    }

    public void pushIn(Player user, Entity pToPush) {
        double pX = user.getLocation().getX();
        double pY = user.getLocation().getY();
        double pZ = user.getLocation().getZ();

        double tX = pToPush.getLocation().getX();
        double tY = pToPush.getLocation().getY();
        double tZ = pToPush.getLocation().getZ();

        double deltaX = tX - pX;
        double deltaY = tY - pY;
        double deltaZ = tZ - pZ;

        pToPush.setVelocity(new Vector(deltaX, deltaY, deltaZ).normalize().multiply(-1.25D).setY(0.3D));
    }
}
