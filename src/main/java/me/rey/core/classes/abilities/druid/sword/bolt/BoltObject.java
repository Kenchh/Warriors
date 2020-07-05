package me.rey.core.classes.abilities.druid.sword.bolt;

import me.rey.core.Warriors;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilEnt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BoltObject {

    Bolt.BoltProfile bo;

    Location loc;
    Location origin;

    boolean onlyvisual;

    boolean incurve = false;

    Location locWithNoCurve;
    double travelDistance = 20;
    double travelledDistanceSinceLastCurve = 0;
    double maxCurveDistance = 5;

    HashMap<UUID, Integer> stacks;
    HashMap<UUID, Long> stackdecay;

    ArrayList<UUID> damagedplayers = new ArrayList<UUID>();

    public BoltObject(Bolt.BoltProfile bo, HashMap<UUID, Integer> stacks, HashMap<UUID, Long> stackdecay, boolean onlyvisual, Location origin) {
        this.bo = bo;
        this.onlyvisual = onlyvisual;
        this.origin = origin;
        this.loc = origin.clone();

        this.stacks = stacks;
        this.stackdecay = stackdecay;

        this.travelDistance = bo.maxTravelDistance * (bo.charge/bo.maxcharge);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(!destroy()) {
                    while(loc.distance(origin) < travelDistance && !destroy()) {
                        tick();
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Warriors.getInstance(), 1L, 1L);

    }

    public void tick() {

        checkCollision();

        double yMultiplier = Math.sqrt(1-Math.pow(UtilBlock.getYCordsMultiplierByPitch(loc.getPitch()), 2));
        loc.setX(UtilBlock.getXZCordsFromDegree(loc, yMultiplier, loc.getYaw() + 90)[0]);
        loc.setY(loc.getY() + UtilBlock.getYCordsMultiplierByPitch(loc.getPitch()));
        loc.setZ(UtilBlock.getXZCordsFromDegree(loc, yMultiplier, loc.getYaw() + 90)[1]);

    }

    public void checkCollision() {

        if(onlyvisual) {
            return;
        }

        Location collisionloc = loc.clone();
        collisionloc.setY(loc.getY() - 2.0);
        for(Entity e : collisionloc.getWorld().getNearbyEntities(collisionloc, bo.hitbox, bo.hitbox + 1, bo.hitbox)) {
            if(e instanceof LivingEntity) {
                if(e == bo.shooter || damagedplayers.contains(e.getUniqueId()))
                    continue;

                if(e instanceof Player) {
                    Player p = (Player) e;
                    if(bo.user.getTeam().contains(p)) {
                        continue;
                    }
                }

                damagedplayers.add(e.getUniqueId());

                if(stacks.containsKey(bo.shooter.getUniqueId())) {
                    if(stacks.get(bo.shooter.getUniqueId()) < bo.maxstacks) {
                        stacks.replace(bo.shooter.getUniqueId(), stacks.get(bo.shooter.getUniqueId()) + 1);
                    }
                }

                if(stackdecay.containsKey(bo.shooter.getUniqueId()) == false) {
                    stackdecay.put(bo.shooter.getUniqueId(), 100L);
                } else {
                    stackdecay.replace(bo.shooter.getUniqueId(), 100L);
                }
                UtilEnt.damage(bo.baseDamage+bo.level*bo.damagePerLevel+bo.damagePerStack*stacks.get(bo.shooter.getUniqueId()), "Lightning Bolt", (LivingEntity) e, bo.shooter);
            }
        }
    }

    public boolean destroy() {

        if((UtilBlock.airFoliage(loc.getBlock()) && loc.distance(origin) < travelDistance) || (loc.getBlock().isLiquid() && loc.distance(origin) < travelDistance)) {

            return false;
        }

        if(!onlyvisual) {
            if (damagedplayers.isEmpty()) {
                stacks.replace(bo.shooter.getUniqueId(), 1);
            }
        }

        return true;
    }

}
