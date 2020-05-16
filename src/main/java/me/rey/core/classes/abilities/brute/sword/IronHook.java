package me.rey.core.classes.abilities.brute.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import me.rey.core.utils.BlockLocation;
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
                            ih.hook.remove();
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
        Item hook;

        Entity hookedentity = null;

        int ticksalive = 0;

        /* Charging hook */
        final double baseChargeCooldown = 5.5;
        final double subtractPerLevel = 0.5;

        /* Throwing the hook */
        final double throwBaseV = 0.5;
        final double throwChargeV = 0.5;
        final double throwLevelMultiplier = 0.25;

        /* Hook grabbing entity */
        final double grabChargeV = 0.75;
        final double grabLevelMultiplier = 0.25;
        final double grabBaseKnockup = 0.5;
        final double grabKnockupLevelMultiplier = 0.5;
        final double hitbox = 0.5;

        final int maxticksalive = 40;

        public IronHookObject(Player p, int level) {
            this.p = p;
            this.level = level;
            this.maxchargeticks = (int) (baseChargeCooldown-subtractPerLevel*level) * 20;
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
                hook = p.getWorld().dropItem(loc, new ItemStack(Material.TRIPWIRE_HOOK));
                direction = p.getLocation().getDirection();
                ItemStack is = hook.getItemStack();
                ItemMeta im = is.getItemMeta();
                im.setDisplayName(ChatColor.RED + "IronHook#" + p.getName());
                is.setItemMeta(im);
                hook.setItemStack(is);
                hook.setVelocity(direction.normalize().multiply(throwBaseV+(throwChargeV*charge)*(1+level*throwLevelMultiplier)).setY(direction.getY() + 0.2));
                thrown = true;
            }
        }

        public void checkForCollision() {
            ticksalive++;

            if(hookedentity != null) {
                return;
            }

            for(Entity e : hook.getNearbyEntities(hitbox, hitbox, hitbox)) {
                if(e instanceof LivingEntity && hookedentity != e) {

                    if(e == p) {
                        continue;
                    }

                    hookedentity = e;
                    hookedentity.setVelocity(direction.normalize().multiply(-((grabChargeV*charge)+level*grabLevelMultiplier)).setY(e.getLocation().getDirection().getY() + grabBaseKnockup+level*grabKnockupLevelMultiplier));
                    p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 2F);
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
            if(hook.isOnGround()) {
                return true;
            }
            return false;
        }

    }

    @EventHandler
    public void onPickUp(PlayerPickupItemEvent e) {
        for(IronHookObject ih : ironhook.values()) {

            if(e.getItem().getItemStack() == null || e.getItem().getItemStack().getItemMeta() == null || e.getItem().getItemStack().getItemMeta().getDisplayName() == null) {
                return;
            }

            if(ih.hook.getItemStack().getItemMeta().getDisplayName() == e.getItem().getItemStack().getItemMeta().getDisplayName()) {
                e.setCancelled(true);
            }
        }
    }

}
