package me.rey.core.classes.abilities.druid.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.energy.EnergyHandler;
import me.rey.core.enums.State;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.gui.Gui;
import me.rey.core.items.Throwable;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilEnt;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class Blaze extends Ability implements IConstant {

    public Blaze() {
        super(202, "Blaze", ClassType.GOLD, AbilityType.SWORD, 1, 5, 0.0, Arrays.asList(
                "Right click to shoot fire, dealing ",
                "damage and setting them on fire for",
                "<variable>0.6+0.2*l</variable> (+0.2) seconds.",
                "",
                "Energy: <variable>32-l</variable>"
        ));
        this.setEnergyCost(32);
        this.setIgnoresCooldown(true);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {
        this.setEnergyCost(0);

        if(!p.isBlocking()) {
            return false;
        }

        this.setEnergyCost((32-level)/20);
        u.consumeEnergy((32-level)/20);

        p.getWorld().playSound(p.getLocation(), Sound.GHAST_FIREBALL, 0.5F, 1.2F);

        new BlazeObject(p, u, level);

        return true;
    }

    class BlazeObject {

        final double baseVelocity = 0.5;
        final double velocityPerLevel = 0.15;

        final double igniteDuration = 0.6;
        final double igniteDurationPerLevel = 0.2;

        public BlazeObject(Player p, User user, int level) {
            Throwable fire = new Throwable(new Gui.Item(Material.BLAZE_POWDER).setLore(Arrays.asList(p.getName() + System.currentTimeMillis())), false);
            fire.fire(p.getEyeLocation(), p.getLocation().getDirection(), baseVelocity+level*velocityPerLevel, 0);

            new BukkitRunnable() {
                @Override
                public void run() {

                    fire.destroyWhenOnGround();

                    if(fire.destroy) {
                        this.cancel();
                        return;
                    }

                    fire.getEntityitem().getWorld().spigot().playEffect(fire.getEntityitem().getLocation(), Effect.LAVADRIP);

                    for(Entity e : fire.getEntityitem().getNearbyEntities(0.5, 0.5, 0.5)) {

                        if(e instanceof LivingEntity) {

                            LivingEntity le = (LivingEntity) e;

                            if(le instanceof Player) {
                                if(user.getTeam().contains(le) || le == p) {
                                    continue;
                                }
                            }

                            UtilEnt.damage(0.5, "Blaze", le, p);

                            le.setFireTicks((int) (20 * (igniteDuration + igniteDurationPerLevel)));

                            fire.destroy();
                            this.cancel();
                            return;
                        }
                    }
                }
            }.runTaskTimer(Warriors.getInstance(), 1L, 1L);

        }
    }

}
