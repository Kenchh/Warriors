package me.rey.core.classes.abilities.brute.sword;

import me.kenchh.main.Eclipse;
import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.gui.Gui;
import me.rey.core.items.Throwable;
import me.rey.core.players.User;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class IronHook extends Ability {
	
    public IronHook() {
        super(601, "Iron Hook", ClassType.DIAMOND, AbilityType.SWORD, 1, 3, 10, Arrays.asList(
                "Right click to charge up an iron hook,",
                "release hook by releasing right click.",
                "",
                "Fully charging increases the hook's accuracy",
                "and pull strength by <variable>25+25*l</variable>% (+25%).",
                "",
                "Charge time: <variable>5.5-0.5*l</variable> (-0.5)",
                "",
                "Recharge: <variable>10.5-0.5*l</variable> (-0.5)"
        ));
    }

    public HashMap<UUID, IronHookObject> ironhook = new HashMap<UUID, IronHookObject>();

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        if(ironhook.containsKey(p.getUniqueId()) == false) {
            ironhook.put(p.getUniqueId(), new IronHookObject(p, level));
            this.setCooldownCanceled(true);

            new BukkitRunnable() {
                @Override
                public void run() {

                    IronHookObject ih = ironhook.get(p.getUniqueId());

                    ih.updateLoc();

                    if(p.isBlocking() && ih.charged == false) {
                        ih.charge();
                    } else {
                        ih.charged = true;
                        ih.throwHook();

                        if (ih.thrown && hasCooldown(p) == false) {
                            setCooldown(10.5 - 0.5 * level);
                            applyCooldown(p);
                        }

                        if (ih.destroy() || ih.tooOld()) {
                            ih.hook.destroy();
                            ironhook.remove(p.getUniqueId());
                            this.cancel();
                            return;
                        }

                        ih.checkForCollision();

                    }

                }
            }.runTaskTimer(Warriors.getInstance(), 0L, 1L);

        }

        return true;
    }

    class IronHookObject {

        Player p;
        int level;
        Location loc;
        Vector direction;

        int chargeticks = 1;
        double charge = 0.01;
        boolean charged;

        int maxchargeticks;

        boolean thrown = false;
        Throwable hook;

        Entity hookedentity = null;

        int ticksalive = 0;

        /* Charging hook */
        final double baseChargeCooldown = 5.5;
        final double subtractPerLevel = 0.5;

        /* Throwing the hook */
        final double throwBaseV = 0.75;
        final double throwChargeV = 0.5;
        final double throwLevelMultiplier = 0.5;

        /* Hook grabbing entity */
        final double grabChargeV = 0.75;
        final double grabLevelMultiplier = 0.25;
        final double grabBaseKnockup = 0.3;
        final double grabKnockupLevelMultiplier = 0.10;
        final double hitbox = 0.5;

        final int maxticksalive = 40;

        public IronHookObject(Player p, int level) {
            this.p = p;
            this.level = level;
            this.maxchargeticks = (int) (baseChargeCooldown-subtractPerLevel*level) * 20;
            this.hook = new Throwable(new Gui.Item(Material.TRIPWIRE_HOOK), false);
        }

        public void updateLoc() {
            this.loc = new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY() + 1, p.getLocation().getZ());
        }

        public void charge() {
            if(chargeticks < maxchargeticks) {
                chargeticks++;
                charge = (double) chargeticks/(double) maxchargeticks;
                if(chargeticks*2+20 < maxchargeticks)
                    p.playSound(p.getLocation(), Sound.NOTE_PIANO, 1F, 2F*(((float) (chargeticks*2+20)/(float) maxchargeticks)));
            }
        }

        public void throwHook() {
            if(thrown == false) {
                direction = p.getLocation().getDirection();
                p.getWorld().playSound(p.getLocation(), Sound.IRONGOLEM_THROW, 2f, 0.8f);
                hook.fire(p.getEyeLocation(), direction,throwBaseV+(throwChargeV*charge)*(1+level*throwLevelMultiplier), direction.getY(), 0.2);
                thrown = true;
            }
        }

        public void checkForCollision() {
            ticksalive++;

            if(hookedentity != null) {
                return;
            }

            hook.getEntityitem().getWorld().playSound(hook.getEntityitem().getLocation(), Sound.FIRE_IGNITE, 1.4F, 0.8F);
            hook.getEntityitem().getWorld().spigot().playEffect(hook.getEntityitem().getLocation(), Effect.CRIT, 0, 0, 0, 0, 0, 0, 1, 50);

            for(Entity e : hook.getEntityitem().getNearbyEntities(hitbox, hitbox, hitbox)) {
                if(e instanceof LivingEntity && hookedentity != e) {

                    if(e == p) {
                        continue;
                    }

                    hookedentity = e;
                    if(hookedentity instanceof Player) {
                        Eclipse.getInstance().api.setCheckMode((Player) hookedentity, "Iron Hook", 2);
                    }
                    hookedentity.setVelocity(direction.normalize().multiply(-((grabChargeV*charge)+level*grabLevelMultiplier)).setY(0.5 + direction.normalize().getY() * grabBaseKnockup+level*grabKnockupLevelMultiplier));
                    p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 1.5F);
                    break;
                }
            }
        }

        public boolean tooOld() {
            if(ticksalive >= maxticksalive) {
                return true;
            }
            return false;
        }

        public boolean destroy() {
            if(hook.getEntityitem().isOnGround()) {
                return true;
            }
            return false;
        }

    }

}
