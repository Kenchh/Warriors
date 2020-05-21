package me.rey.core.classes.abilities.shaman.spade;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.SoundEffect;
import me.rey.core.gui.Gui;
import me.rey.core.items.Throwable;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilEnt;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Paralysis extends Ability {
    public Paralysis() {
        super(522, "Paralysis", ClassType.GREEN, AbilityType.SPADE, 1, 3, 8, Arrays.asList(
                "Shoot a slime ball in the air, rooting enemies",
                "who got hit by it into the ground for <variable>2</variable> seconds",
                "and dealing <variable>4</variable> damage.",
                "",
                "Energy: <variable>42-2*l</variable>",
                "",
                "Recharge: <variable>8.25-0.25*l</variable>"
        ));
        this.setEnergyCost(42);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {
        this.setEnergyCost(42-2*level);
        this.setCooldown(8.25-level*0.25);

        ParalysisObject slimeball = new ParalysisObject(p, u);
        slimeball.shoot();

        new BukkitRunnable() {
            @Override
            public void run() {
                if(slimeball.ball.destroy) {
                    this.cancel();
                    return;
                } else {
                    slimeball.tick();
                }
            }
        }.runTaskTimer(Warriors.getInstance(), 0L, 1L);

        return false;
    }

    class ParalysisObject {

        Player shooter;
        User user;
        Throwable ball;

        public ParalysisObject(Player p, User u) {
            this.shooter = p;
            this.user = u;
            this.ball = new Throwable(new Gui.Item(Material.SLIME_BALL), false);
        }

        public void shoot() {
            if(ball.fired == false) {
                ball.fired = true;
                shooter.getWorld().playSound(shooter.getLocation(), Sound.SLIME_ATTACK, 1F, 1F);
                ball.fire(shooter.getEyeLocation(), shooter.getLocation().getDirection(), 1, 0);
            }
        }

        public void tick() {
            ball.destroyWhenOnGround();
            checkCollision();
        }

        public void checkCollision() {
            if(ball.fired == false) {
                return;
            }

            ball.getEntityitem().getWorld().spigot().playEffect(ball.getEntityitem().getLocation(), Effect.SLIME);

            for(Entity e : ball.getEntityitem().getNearbyEntities(0.5, 0.5, 0.5)) {
                if(e instanceof LivingEntity && e != shooter && user.getTeam().contains(e) == false) {
                    ball.destroy();

                    LivingEntity le = (LivingEntity) e;

                    UtilEnt.damage(4.0, "Paralysis", le, shooter);

                    le.getLocation().getWorld().playSound(le.getLocation(), Sound.DIG_GRASS, 1F, 1F);
                    le.getLocation().getWorld().playSound(le.getLocation(), Sound.DIG_GRASS, 1F, 1F);
                    for(double radius=2; radius>=1; radius-=0.25) {
                        for (Location loc : UtilBlock.circleLocations(le.getLocation(), radius, 5)) {
                            loc.getWorld().spigot().playEffect(loc, Effect.CRIT, 0, 0, 0, 0, 0, 0, 0, 30);
                        }
                    }

                    le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 200));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 200));

                }
            }

        }

    }

}
