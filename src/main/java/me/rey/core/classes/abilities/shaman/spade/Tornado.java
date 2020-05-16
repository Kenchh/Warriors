package me.rey.core.classes.abilities.shaman.spade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.kenchh.main.Eclipse;
import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.SoundEffect;
import me.rey.core.packets.ActionBar;
import me.rey.core.players.User;
import me.rey.core.pvp.ToolType;
import me.rey.core.utils.BlockLocation;

public class Tornado extends Ability {
    public Tornado() {
        super(521, "Tornado", ClassType.GREEN, AbilityType.SPADE, 1, 3, 13.0, Arrays.asList(
        		"Charge up a tornado in a direction",
                "that will knock up and slow all ",
                "enemies for <variable>2+l</variable> (+1) Seconds hit by it.",
                "",
                "Recharge: <variable>13.0 - l*2</variable> (-2) Seconds"
        		));
    }

    public HashMap<UUID, TornadoObject> tornado = new HashMap<>();

    public boolean run(Player p, ToolType toolType, boolean messages, Object... conditions) {

        User user = new User(p);

        if(user.getWearingClass() != this.getClassType() || !this.matchesAbilityTool(this.match(p.getItemInHand()))) {
            return super.run(p, toolType, messages, conditions);
        }

        if(tornado.containsKey(p.getUniqueId())) {
            TornadoObject t = tornado.get(p.getUniqueId());
            t.updatePrepareTick();
        } else {
            TornadoObject to = new TornadoObject(p, this, 1, p.getLocation());
            tornado.put(p.getUniqueId(), to);
            SoundEffect.playCustomSound(p.getLocation(), "preparetornado", 2F, 1F);
        }

        return super.run(p, toolType, messages, conditions);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        if(tornado.containsKey(p.getUniqueId()) == false) {
            return false;
        }
        this.setCooldownCanceled(true);
        TornadoObject ot = tornado.get(p.getUniqueId());

        if(ot.preparing == false) {
            return false;
        }

        final double distance = 21 + (level * 3);
        ot.traveldistance = distance;
        ot.slowduration += level*20;
        ot.preparing = false;

        Location origin = p.getLocation().clone();
        new BukkitRunnable() {
            @Override
            public void run() {

                TornadoObject t = tornado.get(p.getUniqueId());

                t.updateTicks();
                boolean prepare = t.checkPrepare();
                if(prepare) {
                    setCooldown(13.0 - level*2);
                    applyCooldown(p);
                }

                Location found = t.move().clone();
                found.setY(origin.getY());
                
                if (found.distance(origin) >= t.traveldistance*t.charge) {
                    tornado.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }


                t.whirl();
                t.knockup(p);

            }
        }.runTaskTimer(Warriors.getInstance(), 0L, 1L);

        return true;
    }

    class TornadoObject {

    	double ticks;

    	double lastpreparetick;
        boolean preparing = true;

        double traveldistance;
        int slowduration = 40;

        double charge = 0.01;

        double cordsAdders[];

        Player p;
        Ability a;

        Location loc;

        final double spread = 1.5;
        final int particlecount = 20; /* INDIRECT PROPORTIONAL! Lower value -> Higher count/particle thickness */
        final double maxheight = 5.55;
        final double travelspeed = 0.5;
        final double rotationspeed = 20;
        final double radius = 2;
        final double knockup = 1;
        final double maxchargeticks = 60;
        //final double maxticks = 80;
        
        ArrayList<LivingEntity> knockUped = new ArrayList<>();

        public TornadoObject(Player p, Ability a, double traveldistance, Location startloc) {
            this.p = p;
            this.a = a;
            this.loc = startloc;
            this.traveldistance = traveldistance;
            
            this.ticks = 1;
        }

        public void updateTicks() {
            ticks += 1;
        }

        public void updatePrepareTick() {
            if(lastpreparetick != -1) {
                lastpreparetick = ticks;
                charge = ticks/maxchargeticks;
                
                ActionBar.getChargingBar(a.getName(), charge * 100).send(p);
            }
        }

        public boolean checkPrepare() {
            if((ticks - lastpreparetick > 6 || ticks >= maxchargeticks) && lastpreparetick != -1) {
                lastpreparetick = -1;

                Block targetblock = null;
                double direction = p.getLocation().getYaw() + 90;

                for(int i=0; i<=30; i++) {
                    if(BlockLocation.getTargetBlock(p, i).getType().isSolid()) {
                        targetblock = BlockLocation.getTargetBlock(p, i);
                        break;
                    }
                }

                if(targetblock != null) {
                    double deltaX = targetblock.getX() - loc.getX();
                    double deltaZ = targetblock.getZ() - loc.getZ();
                    direction = Math.toDegrees(Math.atan(deltaZ/deltaX));
                    if(deltaX < 0) {
                        direction += 180;
                    }
                }

                cordsAdders = BlockLocation.getXZCordsMultipliersFromDegree(direction);
                SoundEffect.playCustomSound(loc, "tornado", 2F, 1F);
                return true;
            }
            return false;
        }

        public Location move() {
            if(lastpreparetick == -1) {
                loc.setX(loc.getX() + cordsAdders[0] * travelspeed * charge);
                if(BlockLocation.highestLocation(loc) != null) {
                    loc.setY(BlockLocation.highestLocation(loc).getY());
                }
                loc.setZ(loc.getZ() + cordsAdders[1] * travelspeed * charge);
            }
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

                ploc.setX(loc.getX() + mults[0] * degree/720 * spread * charge);
                ploc.setY(loc.getY() + degree * ((maxheight * charge)/1000));
                ploc.setZ(loc.getZ() + mults[1] * degree/720 * spread * charge);

                ploc.getWorld().spigot().playEffect(ploc, Effect.SNOW_SHOVEL, 0, 0, 0, 0, 0, 0, 0, 50);

            }

        }

        public void knockup(Player p) {

            if(lastpreparetick != -1) {
                return;
            }

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
                        	if(le instanceof Player) Eclipse.getInstance().api.setCheckMode((Player) le, "tornado", 1.5); /* ANTICHEAT CHECK */

                            le.setVelocity(le.getVelocity().setY(knockup*charge));
                            (le).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowduration, 0));
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
