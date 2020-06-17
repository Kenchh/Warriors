package me.rey.core.classes.abilities.druid.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.energy.EnergyHandler;
import me.rey.core.enums.State;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.packets.ActionBar;
import me.rey.core.players.User;
import me.rey.core.pvp.ToolType;
import me.rey.core.utils.ChargingBar;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilParticle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LightningBolt extends Ability {

    EnergyHandler handler = new EnergyHandler();

    public LightningBolt() {
        super(200, "Lightning Bolt", ClassType.GOLD, AbilityType.SWORD, 1, 5, 1.1, Arrays.asList(
                "lithntming bo lgd"
        ));
    }

    public HashMap<UUID, BoltObject> bolts = new HashMap<UUID, BoltObject>();
    public HashMap<UUID, Integer> stacks = new HashMap<UUID, Integer>();

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        if(bolts.containsKey(p.getUniqueId()))
            return false;

        if(bolts.containsKey(p.getUniqueId()) == false) {
            BoltObject bolt = new BoltObject(p, level);
            bolts.put(p.getUniqueId(), bolt);
        }

        BoltObject bolt = bolts.get(p.getUniqueId());

        if(bolt.charge == -1)
            return false;

        this.setCooldownCanceled(true);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(p.isBlocking()) {
                    if(bolt.charge < bolt.maxcharge) {
                        bolt.charge++;
                        p.playSound(p.getLocation(), Sound.CREEPER_HISS, 1F, 0.25F + 2.5F * (float) (bolt.charge/bolt.maxcharge));
                        new ActionBar(new ChargingBar(ChargingBar.ACTIONBAR_BARS, bolt.maxcharge - bolt.charge + 1, bolt.maxcharge).
                                getBarString().replace(ChatColor.GREEN + "", ChatColor.YELLOW + "").replace(ChatColor.RED + "", ChatColor.WHITE + "")).send(p);
                    } else {
                        bolt.shoot();
                        u.consumeEnergy(39-level*3);
                        applyCooldown(p);
                        bolts.remove(p.getUniqueId());
                        this.cancel();
                    }
                } else {
                    if(bolt.charge > bolt.maxcharge * 0.5) {
                        bolt.shoot();
                        u.consumeEnergy(39 - level * 3);
                        applyCooldown(p);
                    }
                    bolts.remove(p.getUniqueId());
                    this.cancel();
                }
            }
        }.runTaskTimer(Warriors.getInstance(), 1L, 1L);

        return true;
    }

    class BoltObject {

        Player shooter;
        Location loc;
        Location origin;

        int level;

        double charge = 0;

        boolean incurve = false;

        Location locWithNoCurve;
        double travelledDistanceSinceLastCurve = 0;
        double maxCurveDistance = 5;
        double maxDistanceUntilCurve = 0.5;

        final double hitbox = 0.7;
        final double maxcharge = 1.1 * 20;
        final double particledensity = 3;
        final double maxTravelDistance = 50;

        public BoltObject(Player shooter, int level) {
            this.shooter = shooter;
            this.level = level;
        }

        public void shoot() {
            origin = shooter.getEyeLocation().clone();
            loc = origin.clone();
            charge = -1;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if(!destroy()) {
                        tick();
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Warriors.getInstance(), 1L, 1L);
        }

        public void tick() {

            Random randomsound = new Random();
            int rs = randomsound.nextInt(3) + 1;

            SoundEffect.playCustomSound(loc, "lightningbolt" + rs, 1F, 1F);
            playParticle();
            checkCollision();

        }

        public void checkCollision() {
            for(Entity e : loc.getWorld().getNearbyEntities(loc, hitbox, hitbox, hitbox)) {
                if(e instanceof LivingEntity) {
                    if(e == shooter)
                        return;

                    if(e instanceof Player) {
                        Player p = (Player) e;
                        User u = new User(p);
                        if(u.getTeam().contains(e))
                            return;
                    }

                    UtilEnt.damage(4.5+level*0.5, "Lightning Bolt", (LivingEntity) e, shooter);
                }
            }
        }

        public void playParticle() {
            double addX = UtilBlock.getXZCordsMultipliersFromDegree(loc.getYaw() + 90)[0] / particledensity;
            double addY = UtilBlock.getYCordsMultiplierByPitch(loc.getPitch());
            double addZ = UtilBlock.getXZCordsMultipliersFromDegree(loc.getYaw() + 90)[1] / particledensity;

            if(!incurve) {

                locWithNoCurve = loc.clone();

                while(travelledDistanceSinceLastCurve <= maxDistanceUntilCurve) {

                    travelledDistanceSinceLastCurve = locWithNoCurve.distance(loc);

                    loc.setX(loc.getX() + addX);
                    loc.setY(loc.getY() + addY);
                    loc.setZ(loc.getZ() + addZ);

                    for(int i=1;i<=5;i++) {
                        UtilParticle.playColoredParticle(loc, 255, 255, 102);
                        UtilParticle.playColoredParticle(loc, 255, 255, 255);
                    }
                }
                incurve = true;
            } else {
                Location bloc = loc.clone(); /* Setting start break point */

                Random rd = new Random();
                double randomaddDegree = rd.nextInt(8) + 5; /* Minimum curve 10, max 25 */
                Random positive = new Random();
                if (!positive.nextBoolean()) {
                    randomaddDegree = -randomaddDegree;
                }
                double degree = (loc.getYaw() + 90) + randomaddDegree;

                while(bloc.distance(loc) <= maxCurveDistance) {
                    double curveaddX = UtilBlock.getXZCordsMultipliersFromDegree(degree)[0];
                    double curveaddZ = UtilBlock.getXZCordsMultipliersFromDegree(degree)[1];

                    loc.setX(loc.getX() + curveaddX);
                    loc.setY(loc.getY() + addY);
                    loc.setZ(loc.getZ() + curveaddZ);

                    for(int i=1;i<=5;i++) {
                        UtilParticle.playColoredParticle(loc, 255, 255, 102);
                        UtilParticle.playColoredParticle(loc, 255, 255, 255);
                    }
                }

                bloc = loc.clone(); /* Setting middle break point */
                degree = (loc.getYaw() + 90) - randomaddDegree*2; /* Reflection */

                while(bloc.distance(loc) <= maxCurveDistance) {
                    double curveaddX = UtilBlock.getXZCordsMultipliersFromDegree(degree)[0];
                    double curveaddZ = UtilBlock.getXZCordsMultipliersFromDegree(degree)[1];

                    loc.setX(loc.getX() + curveaddX);
                    loc.setY(loc.getY() + addY);
                    loc.setZ(loc.getZ() + curveaddZ);

                    for(int i=1;i<=5;i++) {
                        UtilParticle.playColoredParticle(loc, 255, 255, 102);
                        UtilParticle.playColoredParticle(loc, 255, 255, 255);
                    }
                }

                travelledDistanceSinceLastCurve = 0;
                incurve = false;
            }
        }

        public boolean destroy() {

            if(loc.getBlock().isLiquid() == false && loc.distance(origin) < maxTravelDistance) {
                return false;
            }

            return true;
        }


    }

}
