package me.rey.core.classes.abilities.shaman.spade;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilParticle;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Miasma extends Ability {
    public Miasma() {
        super(523, "Miasma", ClassType.GREEN, AbilityType.SPADE, 1, 4, 10, Arrays.asList(
                "Miasma"
        ));
        this.setEnergyCost(55, 2);
    }

    private HashMap<UUID, Integer> particleticks = new HashMap<UUID, Integer>();

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        u.consumeEnergy(55-2*level);

        double radius = 5 + level;
        double duration = 4 + 0.5*level;

        for(Entity e : UtilBlock.getEntitiesInCircle(p.getLocation(), radius)) {
            if(e instanceof LivingEntity) {
                LivingEntity le = (LivingEntity) e;

                if(le instanceof Player) {
                    if(u.getTeam().contains(le) || le == p) {
                        continue;
                    }
                }

                le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) duration * 20, 1));
                le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) duration * 20, 0));

            }
        }

        /* Particles */

        particleticks.put(p.getUniqueId(), 0);

        new BukkitRunnable() {
            @Override
            public void run() {

                if(particleticks.get(p.getUniqueId()) < 10) {
                    particleticks.replace(p.getUniqueId(), particleticks.get(p.getUniqueId()) + 1);
                } else {
                    particleticks.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }

                for(int r=(int) radius; r>=1; r-=2) {

                    p.getWorld().playSound(p.getLocation(), Sound.HORSE_BREATHE, 1F, 0.75F);

                    for (double degree=0; degree<=360; degree+=40) {

                        Random ry = new Random();
                        double randomY = (ry.nextInt(200) + 100) / 100;

                        Location particle = p.getLocation().clone();
                        particle.setX(UtilBlock.getXZCordsFromDegree(particle, r, degree + particleticks.get(p.getUniqueId())*10)[0]);
                        particle.setY(particle.getY() + randomY);
                        particle.setZ(UtilBlock.getXZCordsFromDegree(particle, r, degree + particleticks.get(p.getUniqueId())*10)[1]);

                        UtilParticle.playColoredParticle(particle, 240, 200, 250);

                    }
                }
            }
        }.runTaskTimer(Warriors.getInstance(), 1L, 1L);

        return true;
    }
}
