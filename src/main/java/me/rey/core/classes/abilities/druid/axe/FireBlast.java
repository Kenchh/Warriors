package me.rey.core.classes.abilities.druid.axe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import me.rey.core.utils.UtilBlock;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;

public class FireBlast extends Ability {

	private HashMap<UUID, FireProfile> fireballs;
	
	public FireBlast() {
		super(211, "Fire Blast", ClassType.GOLD, AbilityType.AXE, 1, 5, 12.00, Arrays.asList(
				"Launch a fireball which explodes on impact",
				"dealing large knockback to enemies within",
				"<variable>0.5*l+3</variable> (+0.5) Blocks range. Also ignites enemies",
				"for up to <variable>2*l+2</variable> (+2) seconds.",
				"",
				"Energy: <variable>0-4*l+54</variable> (-4)",
				"Recharge: <variable>0-1*l+13</variable> (-1) Seconds"));
		
		this.fireballs = new HashMap<>();
		this.setEnergyCost(54, 4);
	}

	@Override
	protected boolean execute(User u, final Player p, int level, Object... conditions) {

		fireballs.put(p.getUniqueId(), new FireProfile(p, u, level));

		this.setCooldown(-1*level+13);
		return true;
	}
	
	@EventHandler
	public void onEntityDamage(ProjectileHitEvent e) {
		if(!(e.getEntity() instanceof Fireball && e.getEntityType() == EntityType.FIREBALL)) return;
		Fireball fireball = (Fireball) e.getEntity();
		
		FireProfile fp = null;
		for(UUID u : fireballs.keySet()) {
			if(fireballs.get(u).fireball == fireball) {
				fp = fireballs.get(u);
			}
		}

		if(fp == null) {
			return;
		}

		Player p = fp.shooter;

		for(Location cloc : UtilBlock.circleLocations(fp.fireball.getLocation(), 3.0 + (double) fp.level/2, 30)) {
			for(int i=0;i<10;i++)
				cloc.getWorld().spigot().playEffect(cloc, Effect.FLAME, 0, 0, 0, 0, 0, 1F, 1, 30);
		}

		Location loc = e.getEntity().getLocation();
		final List<Entity> entities = (List<Entity>)loc.getWorld().getNearbyEntities(loc, 3.0 + (double) fp.level/2, 3.0, 3.0 + (double) fp.level/2);
        for (final Entity d : entities) {
            if (d instanceof LivingEntity) {
                final LivingEntity dc = (LivingEntity)d;
                if (dc instanceof Player) {
                    final Player z2 = (Player)dc;
                    if (z2 != p && fp.u.getTeam().contains(z2) == false) {
						dc.damage(4, p);
						dc.setFireTicks((2+fp.level*2)*20);
					}
                } else {
					dc.damage(4, p);
					dc.setFireTicks((2+fp.level*2)*20);
				}

				for(Location cloc : UtilBlock.circleLocations(fp.fireball.getLocation(), 1, 45)) {
					cloc.getWorld().spigot().playEffect(cloc, Effect.LAVA_POP);
				}

                final Vector vc = dc.getLocation().toVector().subtract(loc.toVector());
                dc.setVelocity(vc.normalize().multiply(0.6));
                dc.setVelocity(new Vector(dc.getVelocity().getX(), 1, dc.getVelocity().getZ()));
            }
        }
        
        this.fireballs.remove(p.getUniqueId());
	}

	class FireProfile {

		Player shooter;
		User u;
		int level;
		Fireball fireball;

		public FireProfile(Player p, User u, int level) {

			shooter = p;
			this.u = u;
			this.level = level;

			Location direction = p.getEyeLocation().toVector().add(p.getLocation().getDirection().multiply(1)).toLocation(p.getWorld(),
					p.getLocation().getYaw(), p.getLocation().getPitch());
			fireball = p.launchProjectile(Fireball.class);

			fireball.setVelocity(direction.getDirection());
			fireball.setShooter(p);
			fireball.setFireTicks(0);
			fireball.setYield(0F);
			fireball.setIsIncendiary(false);
			fireball.setVelocity(p.getEyeLocation().getDirection().multiply(1));

			new BukkitRunnable() {
				int seconds = 0;
				@Override
				public void run() {
					if(fireball.isDead()) {
						this.cancel();
						return;
					}

					if(seconds >= 10) {
						this.cancel();
						fireball.remove();
					} else {
						fireball.setTicksLived(1);
					}
					seconds++;
				}

			}.runTaskTimer(Warriors.getPlugin(Warriors.class), 0, 20);
		}

	}

}
