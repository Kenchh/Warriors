package me.rey.core.classes.abilities.shaman.spade;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import me.rey.core.pvp.ToolType;
import me.rey.core.utils.BlockLocation;
import me.rey.core.utils.SoundEffect;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Tornado extends Ability {
    public Tornado() {
        super(521, "Tornado", ClassType.GREEN, AbilityType.SPADE, 1, 3, 5.0, Arrays.asList("adwa"));
    }

    public HashMap<UUID, Integer> prepareTornado = new HashMap<>();
    public HashMap<UUID, TornadoObject> tornado = new HashMap<>();

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        double[] cords = BlockLocation.getXZCordsMultipliersFromDegree(p.getLocation().getYaw() + 90);
        tornado.put(p.getUniqueId(), new TornadoObject(cords[0], cords[1], p.getLocation()));
        SoundEffect.playCustomSound(p.getLocation(), "tornado", 2F, 1F);

        new BukkitRunnable() {
            @Override
            public void run() {

                TornadoObject t = tornado.get(p.getUniqueId());

                if(t.ticks >= t.traveldistance) {
                    tornado.remove(t);
                    this.cancel();
                    return;
                }

                t.updateTicks();
                t.move();

                t.whirl();
                t.knockup(p);

            }
        }.runTaskTimer(Warriors.getInstance(), 0L, 1L);

        return false;
    }

    class TornadoObject {

        double ticks = 1;

        double xMultiplier;
        double zMultiplier;

        Location loc;

        final double spread = 1.5;
        final double particlecount = 20;
        final double height = 5.55;
        final double travelspeed = 0.5;
        final double traveldistance = 30;
        final double rotationspeed = 20;
        final double radius = 2;
        final double knockup = 0.70;

        ArrayList<LivingEntity> knockUped = new ArrayList<>();

        public TornadoObject(double xMultiplier, double zMultiplier, Location startloc) {
            this.xMultiplier = xMultiplier;
            this.zMultiplier = zMultiplier;
            this.loc = startloc;
        }

        public void updateTicks() {
            ticks += 1;
        }

        public void move() {
            loc.setX(loc.getX() + xMultiplier * travelspeed);
            loc.setY(BlockLocation.highestLocation(loc).getY());
            loc.setZ(loc.getZ() + zMultiplier * travelspeed);
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
