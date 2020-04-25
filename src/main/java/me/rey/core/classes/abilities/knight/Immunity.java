package me.rey.core.classes.abilities.knight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;

public class Immunity extends Ability {
	
	private ArrayList<UUID> players;

	public Immunity() {
		super(302, "Immunity", ClassType.IRON, AbilityType.SWORD, 2, 3, 0.00, Arrays.asList(
				"Gain an immunity period where you",
				"negate all incoming damage. This",
				"effect lasts for <variable>5+l</variable> (+1) seconds.",
				"",
				"Recharge: <variable>2.5*l+30</variable> (+2.5) Seconds."
				));
		
		this.players = new ArrayList<>();
	}

	@Override
	protected boolean execute(User u, final Player p, int level, Object... conditions) {
		int seconds = 5+level;
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, seconds * 20 - 4, 6));
		p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, seconds * 20 - 4, 200, false, false));
		
		this.players.add(p.getUniqueId());
		new BukkitRunnable() {
			
			@Override
			public void run() {
				players.remove(p.getUniqueId());
			}
			
		}.runTaskLater(Warriors.getInstance(), seconds * 20);
		
		new BukkitRunnable() {
			boolean up = true;
			
			int height = 2;
			double yIncrement = 0.1;
			double radius = 0.8, a = 0, x = 0, y = 0, z = 0;
			
			@Override
			public void run() {
				if(!players.contains(p.getUniqueId())) {
					Location location = p.getEyeLocation();
					for(double i = 0; i <= Math.PI; i += Math.PI / 10) {
						double radius = Math.sin(i) - 0.2;
						double y = Math.acos(i);
						for(double a = 0; a < Math.PI * 2; a+= Math.PI / 10) {
							double x = Math.cos(a) * radius;
							double z = Math.sin(a) * radius;
							location.add(x, y,z);
							location.getWorld().spigot().playEffect(location, Effect.COLOURED_DUST, 10, 0, 0F, 0F, 0F, 0F, 3, 100);
							location.subtract(x, y, z);
						}
					}
					this.cancel();
				}
				
				Location loc = p.getLocation();
				
				x = Math.cos(a) * radius;
				z = Math.sin(a) * radius;
				
				loc.add(x, y, z);
				loc.getWorld().spigot().playEffect(loc, Effect.FLAME, 0, 0, 0F, 0F, 0F, 0F, 3, 100);
				loc.subtract(x, y, z);
				
				a += 0.3;
				
				if(up) {
					
					if(y >= height) {
						up = false;
						y -= yIncrement;
					} else {
						y += yIncrement;
					}
					
				} else {
					if(y <= 0) {
						up = true;
						y += yIncrement;
					} else {
						y -= yIncrement;
					}
				}
				
				if(a>= 360)
					a = 0;
			}
		}.runTaskTimer(Warriors.getInstance(), 0, 1);
		
		this.sendUsedMessageToPlayer(p, this.getName());
		this.setCooldown(2.5*level+30);
		return true;
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		if(!(this.players.contains(((Player) e.getEntity()).getUniqueId()))) return;
		
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
		if(!(e.getDamager() instanceof Player)) return;
		if(!(this.players.contains(((Player) e.getDamager()).getUniqueId()))) return;
		
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if(!this.players.contains(e.getPlayer().getUniqueId())) return;
		
		Location from = e.getFrom(), to = e.getTo();
		double fx = from.getX(), fy = from.getY(), fz = from.getZ();
		double tx = to.getX(), ty = to.getY(), tz = to.getZ();

		if(fx != tx || fz != tz) {
			if(ty != fy)
				return;
			e.setTo(from);
		}
	}

}
