package me.rey.core.classes.abilities.brute.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
                "Fully Charged Iron Hook's have a velocity",
                "<variable>1.75+0.25*l</variable> times greater than an uncharged hook.",
                "",
                "Charge up time: <variable>5.5-0.5*l</variable>",
                "",
                "Recharge: <variable>10.5-0.5*l</variable>"
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
                    ih.maxchargeticks = (int) (5.5-0.5*level) * 20;

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

                        if (ih.collisioncheckticks >= ih.maxcollisioncheckticks) {
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

        int ticks;
        int chargeticks = 1;
        boolean charged;

        int maxchargeticks = 60;

        boolean thrown = false;
        Item hook;

        Entity hookedentity;

        int collisioncheckticks = 0;
        final int maxcollisioncheckticks = 50;

        public IronHookObject(Player p, int level) {
            this.p = p;
        }

        public void updateLoc() {
            this.loc = new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY() + 1, p.getLocation().getZ());
        }

        public void charge() {
            if(chargeticks < maxchargeticks) {
                chargeticks++;
                if(chargeticks+20 < maxchargeticks)
                    p.playSound(p.getLocation(), Sound.NOTE_PIANO, 1F, 2F*(((float) (chargeticks+20)/(float) maxchargeticks)));
            }
        }

        public void throwHook() {
            if(thrown == false) {
                hook = p.getWorld().dropItem(loc, new ItemStack(Material.TRIPWIRE_HOOK));
                hook.setVelocity(p.getLocation().getDirection().multiply(1*(chargeticks/(maxchargeticks/(1.75+0.25*level)))).setY(0.2));
                thrown = true;
            }
        }

        public void checkForCollision() {
            collisioncheckticks++;
            for(Entity e : hook.getNearbyEntities(0.5, 0.5, 0.5)) {
                if(e instanceof LivingEntity && hookedentity != e) {

                    if(e == p) {
                        continue;
                    }

                    hookedentity = e;
                    e.setVelocity(p.getLocation().getDirection().multiply(-1*(chargeticks/(maxchargeticks/(1.75+0.25*level)))).setY(0.7));
                    p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 2F);
                    break;
                }
            }
        }


    }

}
