package me.rey.core.classes.abilities.wizard.axe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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

	private HashMap<UUID, Player> fireballs;
	
	public FireBlast() {
		super(311, "Fire Blast", ClassType.GOLD, AbilityType.AXE, 1, 5, 12.00, Arrays.asList(
				"Launch a fireball which explodes on impact",
				"dealing large knockback to enemies within",
				"<variable>0.5*l+6</variable> (+0.5) Blocks range. Also ignites enemies",
				"for up to <variable>2*l+2</variable> (+2) seconds.",
				"",
				"Energy: <variable>0-4*l+54</variable> (-4)",
				"Recharge: <variable>0-1*l+13</variable> (-1) Seconds"));
		
		this.fireballs = new HashMap<>();
		this.setEnergyCost(40);
	}

	@Override
	protected boolean execute(User u, final Player p, int level, Object... conditions) {
		Location direction = p.getEyeLocation().toVector().add(p.getLocation().getDirection().multiply(1)).toLocation(p.getWorld(),
				p.getLocation().getYaw(), p.getLocation().getPitch());
		final Fireball fireball = p.launchProjectile(Fireball.class);
		
		fireball.setVelocity(direction.getDirection());
		fireball.setShooter(p);
		fireball.setFireTicks(0);
		fireball.setYield(0F);
		fireball.setIsIncendiary(false);
		fireball.setVelocity(p.getEyeLocation().getDirection().multiply(1));
		
		this.fireballs.put(fireball.getUniqueId(), p);
		
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
		
		this.setCooldown(-1*level+13);
		return true;
	}
	
	@EventHandler
	public void onEntityDamage(ProjectileHitEvent e) {
		if(!(e.getEntity() instanceof Fireball && e.getEntityType() == EntityType.FIREBALL)) return;
		Fireball fireball = (Fireball) e.getEntity();
		if(this.fireballs.containsKey(fireball.getUniqueId())) return;
		
		Player p = this.fireballs.get(fireball.getUniqueId());
		Location loc = e.getEntity().getLocation();
		final List<Entity> entities = (List<Entity>)loc.getWorld().getNearbyEntities(loc, 5.0, 3.0, 5.0);
        for (final Entity d : entities) {
            if (d instanceof LivingEntity) {
                final LivingEntity dc = (LivingEntity)d;
                if (dc instanceof Player) {
                    final Player z2 = (Player)dc;
                    if (z2 == p) {
                        continue;
                    }
                }
                
                dc.damage(4, p);
                final Vector vc = dc.getLocation().toVector().subtract(loc.toVector());
                dc.setVelocity(vc.normalize().multiply(1.2));
                dc.setVelocity(new Vector(dc.getVelocity().getX(), 1.4, dc.getVelocity().getZ()));
            }
        }
        
        this.fireballs.remove(fireball.getUniqueId());
	}

}
