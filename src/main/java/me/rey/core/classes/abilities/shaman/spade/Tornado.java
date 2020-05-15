package me.rey.core.classes.abilities.shaman.spade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.SoundEffect;
import me.rey.core.players.User;
import me.rey.core.utils.BlockLocation;

public class Tornado extends Ability {
    public Tornado() {
        super(521, "Tornado", ClassType.GREEN, AbilityType.SPADE, 1, 3, 5.0, Arrays.asList(
        		"Shoot a tornado that will deal a",
        		"vertical knockback to all enemies",
        		"that go infront of it.", "",
        		"The tornado will travel for a",
        		"distance of <variable>21+(l*3)</variable> blocks."
        		));
    }

    public HashMap<UUID, Integer> prepareTornado = new HashMap<>();
    public HashMap<UUID, TornadoObject> tornado = new HashMap<>();

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {
    	final double distance = 21 + (level * 3);

        double[] cords = BlockLocation.getXZCordsMultipliersFromDegree(p.getLocation().getYaw() + 90);
        tornado.put(p.getUniqueId(), new TornadoObject(distance, cords[0], cords[1], p.getLocation()));
        SoundEffect.playCustomSound(p.getLocation(), "tornado", 2F, 1F);

        Location origin = p.getLocation().clone();
        new BukkitRunnable() {
            @Override
            public void run() {

                TornadoObject t = tornado.get(p.getUniqueId());

                t.updateTicks();
                Location found = t.move().clone();
                found.setY(origin.getY());
                
                if (found.distance(origin) >= t.traveldistance) {
                    tornado.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }


                t.whirl();
                t.knockup(p);

            }
        }.runTaskTimer(Warriors.getInstance(), 0L, 1L);

        return false;
    }

    class TornadoObject {

    	double ticks;
        double xMultiplier;
        double zMultiplier;
        double traveldistance;

        Location loc;

        final double spread = 1.5;
        final double particlecount = 20;
        final double height = 5.55;
        final double travelspeed = 0.5;
        final double rotationspeed = 20;
        final double radius = 2;
        final double knockup = 0.70;
        
        ArrayList<LivingEntity> knockUped = new ArrayList<>();

        public TornadoObject(double traveldistance, double xMultiplier, double zMultiplier, Location startloc) {
            this.xMultiplier = xMultiplier;
            this.zMultiplier = zMultiplier;
            this.loc = startloc;
            this.traveldistance = traveldistance;
            
            this.ticks = 1;
        }

        public void updateTicks() {
            ticks += 1;
        }

        public Location move() {
            loc.setX(loc.getX() + xMultiplier * travelspeed);
            loc.setY(BlockLocation.highestLocation(loc).getY());
            loc.setZ(loc.getZ() + zMultiplier * travelspeed);
            return loc;
        }

        public void whirl() {

            HashMap<Double, double[]> dCords = new HashMap<Double, double[]>();

            for(double degree=0; degree<=720D; degree++) {
                dCords.put(degree, BlockLocation.getXZCordsMultipliersFromDegree(degree-ticks*rotationspeed));
            }

            for(double degree=0; degree<=720D; degree+=particlecount) {
                double[] mults = dCords.get(degree);

                Location ploc = loc.clone();

                ploc.setX(loc.getX() + mults[0] * degree/720 * spread);
                ploc.setY(loc.getY() + degree * (height/1000));
                ploc.setZ(loc.getZ() + mults[1] * degree/720 * spread);

                ploc.getWorld().spigot().playEffect(ploc, Effect.SNOW_SHOVEL, 0, 0, 0, 0, 0, 0, 0, 50);

            }

        }

        public void knockup(Player p) {
            for(Entity e : loc.getWorld().getNearbyEntities(loc, 5, 2, 5)) {
                if (e instanceof LivingEntity) {

                    LivingEntity le = (LivingEntity) e;

                    if(le.getName().equalsIgnoreCase(p.getName()) || knockUped.contains(le)) {
                        continue;
                    }

                    HashMap<Double, double[]> maxmincords = new HashMap<Double, double[]>();

                    for (double degree = 0; degree <= 360; degree++) {
                        maxmincords.put(degree, BlockLocation.getXZCordsFromDegree(loc, radius, degree));
                    }

                    for (double degree = 0; degree <= 90; degree++) {

                        double[] maxcords = maxmincords.get(degree);
                        double[] mincords = maxmincords.get(180 + degree);

                        double maxX = maxcords[0];
                        double maxZ = maxcords[1];

                        double minX = mincords[0];
                        double minZ = mincords[1];

                        if (le.getLocation().getX() <= maxX && le.getLocation().getZ() <= maxZ && le.getLocation().getX() >= minX && le.getLocation().getZ() >= minZ) {
                            le.setVelocity(le.getVelocity().setY(knockup));
                            (le).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                            knockUped.add(le);
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
    }
}
