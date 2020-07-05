package me.rey.core.classes.abilities.druid.sword.bolt;

import me.rey.core.effects.SoundEffect;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilParticle;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class ElectricBolt extends BoltObject {

    Location ploc;

    public ElectricBolt(Bolt.BoltProfile bo, HashMap<UUID, Integer> stacks, HashMap<UUID, Long> stackdecay, boolean onlyvisual, Location origin) {
        super(bo, stacks, stackdecay, onlyvisual, origin);
        ploc = loc.clone();
    }

    @Override
    public void tick() {
        super.tick();

        if(!onlyvisual) {
            Random randomsound = new Random();
            int rs = randomsound.nextInt(3) + 1;
            SoundEffect.playCustomSound(loc, "lightningbolt" + rs, 1F, 1F + 0.2F*(stacks.get(bo.shooter.getUniqueId())-1));
        }

        if(!UtilBlock.airFoliage(ploc.getBlock()) || ploc.distance(origin) >= travelDistance) {
            return;
        }

        double addX = UtilBlock.getXZCordsMultipliersFromDegree(loc.getYaw() + 90)[0] / bo.particledensity;
        double addY = UtilBlock.getYCordsMultiplierByPitch(loc.getPitch());
        double addZ = UtilBlock.getXZCordsMultipliersFromDegree(loc.getYaw() + 90)[1] / bo.particledensity;

        if(!incurve) {

            locWithNoCurve = loc.clone();

            while(travelledDistanceSinceLastCurve <= bo.maxDistanceUntilCurve) {

                checkCollision();

                travelledDistanceSinceLastCurve = locWithNoCurve.distance(loc);

                loc.setX(loc.getX() + addX);
                loc.setY(loc.getY() + addY);
                loc.setZ(loc.getZ() + addZ);

                if(!UtilBlock.airFoliage(ploc.getBlock()) || ploc.distance(origin) >= travelDistance) {
                    return;
                }

                for(int i=1;i<=5;i++) {
                    UtilParticle.playColoredParticle(loc, 255, 255-20*stacks.get(bo.shooter.getUniqueId()), 102-20*stacks.get(bo.shooter.getUniqueId()));
                    UtilParticle.playColoredParticle(loc, 255, 255-20*stacks.get(bo.shooter.getUniqueId()), 255-20*stacks.get(bo.shooter.getUniqueId()));
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

                checkCollision();

                double curveaddX = UtilBlock.getXZCordsMultipliersFromDegree(degree)[0];
                double curveaddZ = UtilBlock.getXZCordsMultipliersFromDegree(degree)[1];

                loc.setX(loc.getX() + curveaddX);
                loc.setY(loc.getY() + addY);
                loc.setZ(loc.getZ() + curveaddZ);

                if(!UtilBlock.airFoliage(ploc.getBlock()) || ploc.distance(origin) >= travelDistance) {
                    return;
                }

                for(int i=1;i<=5;i++) {
                    UtilParticle.playColoredParticle(loc, 255, 255-20*stacks.get(bo.shooter.getUniqueId()), 102-20*stacks.get(bo.shooter.getUniqueId()));
                    UtilParticle.playColoredParticle(loc, 255, 255-20*stacks.get(bo.shooter.getUniqueId()), 255-20*stacks.get(bo.shooter.getUniqueId()));
                }
            }

            bloc = loc.clone(); /* Setting middle break point */
            degree = (loc.getYaw() + 90) - randomaddDegree*2; /* Reflection */

            while(bloc.distance(loc) <= maxCurveDistance) {

                checkCollision();

                double curveaddX = UtilBlock.getXZCordsMultipliersFromDegree(degree)[0];
                double curveaddZ = UtilBlock.getXZCordsMultipliersFromDegree(degree)[1];

                loc.setX(loc.getX() + curveaddX);
                loc.setY(loc.getY() + addY);
                loc.setZ(loc.getZ() + curveaddZ);

                if(!UtilBlock.airFoliage(ploc.getBlock()) || ploc.distance(origin) >= travelDistance) {
                    return;
                }

                for(int i=1;i<=5;i++) {
                    UtilParticle.playColoredParticle(loc, 255, 255-20*stacks.get(bo.shooter.getUniqueId()), 102-20*stacks.get(bo.shooter.getUniqueId()));
                    UtilParticle.playColoredParticle(loc, 255, 255-20*stacks.get(bo.shooter.getUniqueId()), 255-20*stacks.get(bo.shooter.getUniqueId()));
                }
            }

            travelledDistanceSinceLastCurve = 0;
            incurve = false;
        }

    }

}
