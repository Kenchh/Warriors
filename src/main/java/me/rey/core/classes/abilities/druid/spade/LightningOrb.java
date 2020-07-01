package me.rey.core.classes.abilities.druid.spade;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.repo.Shock;
import me.rey.core.gui.Gui.Item;
import me.rey.core.items.Throwable;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;

public class LightningOrb extends Ability {

	final double secondsToStrike = 1.7;
	
	/* Throwing the item */
	final double throwBaseV = 0.5;
	final double throwChargeV = 0.25;
	final double throwLevelMultiplier = 0.1;
	
	public LightningOrb() {
		super(224, "Lightning Orb", ClassType.GOLD, AbilityType.SPADE, 1, 5, 0.00, Arrays.asList(
				"Throw an orb that strikes lightning",
				"on all nearby players within a radius",
				"of <variable>3.5+(0.5*l)</variable> blocks.",
				"",
				"Applies Slowness 2 for 4 seconds to",
				"players and deals <variable>5+l</variable> damage.",
				"",
				"Energy: <variable>56-(l*3)</variable> (-3)",
				"Recharge: <variable>12-l</variable>(-1) Seconds"
				));
		this.setEnergyCost(56, 3);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		
		Throwable lorb = new Throwable(new Item(Material.EYE_OF_ENDER), false);
		Vector vec = (p.getLocation().getDirection().normalize()
				.multiply(throwBaseV + (throwChargeV) * (1 + level * throwLevelMultiplier))
				.setY(p.getLocation().getDirection().getY() + 0.2));
		lorb.fire(p.getEyeLocation(), vec);
		
		new BukkitRunnable() {
			
			int ticks = 0;
			
			@Override
			public void run() {
				boolean wasDirect = Throwable.checkForEntityCollision(lorb, 0.1, 0.1, 0.1) != null;
				
				if(wasDirect) {
					Set<LivingEntity> ents = Throwable.checkForEntityCollision(lorb, 0.1, 0.1, 0.1);
					if(ents.iterator().next().equals(p))
						wasDirect = !wasDirect;
				}
						
				if(wasDirect || ticks >= (secondsToStrike * 20D)) {
					strikeNearby(p, level, lorb.getEntityitem());
					
					lorb.destroy();
					this.cancel();
					return;					
				}
				
				ticks++;
			}
			
		}.runTaskTimer(Warriors.getInstance(), 0, 2);

		this.setCooldown(12-level);
		return true;
	}
	
	private void strikeNearby(Player responsible, int level, org.bukkit.entity.Item item) {
		PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 2 * 4, 1, false, false);
		double radius = 3.5 + (0.5 * level);
		
		Iterator<Entity> nearby = item.getNearbyEntities(radius, radius, radius).iterator();
		while(nearby.hasNext()) {
			Entity found = nearby.next();
			if(!(found instanceof LivingEntity)) continue;
			
			LivingEntity ent = (LivingEntity) found;
			
			User user = new User(responsible);
			if(!user.getTeam().contains(ent)) {
				ent.addPotionEffect(slow);
				new Shock().apply(ent, 2.0D);
				
				ent.getWorld().strikeLightningEffect(ent.getLocation());
				UtilEnt.damage(5 + level, this.getName(), ent, responsible);
			}
			
		}
	}

}
