package me.rey.core.classes.abilities.knight.sword;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.SoundEffect;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;

public class Thrust extends Ability {

	public Thrust() {
		super(302, "Thrust", ClassType.IRON, AbilityType.SWORD, 1, 5, 0.00, Arrays.asList(
				"Right clicking an enemy does <variable>1+(0.5*l)</variable> (+0.5) damage",
				"and deals Slowness 3 for <variable>1+(0.75*l)</variable> (+0.75) seconds.", "",
				"Recharge: <variable>14-l</variable> (-1) Seconds"
				));

		}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		double RANGE = 3., ACCURACY = 0.1D;
		
		double damage = 1 + 0.5 * level;
		double slowness = 1 + 0.75 * level;
		int slowLevel = 3;
		PotionEffect EFFECT = new PotionEffect(PotionEffectType.SLOW, (int) Math.round(20 *slowness), slowLevel-1, false, false);
		
		/*
		 * Getting entities in line of sight and picking closest
		 */
		Set<LivingEntity> inSight = new HashSet<>();
		Location origin = p.getEyeLocation();
		Vector direction = p.getLocation().getDirection();

		for (int i = 1; i <= RANGE; i++) {
			double x = direction.getX() * i, y = direction.getY() * i, z = direction.getZ() * i;
			origin.add(x, y, z);
			
			origin.getWorld().getNearbyEntities(origin, ACCURACY, ACCURACY, ACCURACY).forEach((ent) -> {
				if(ent instanceof LivingEntity) inSight.add((LivingEntity) ent);
			});
			
			origin.subtract(x, y , z);
		}
		
		LivingEntity toThrust = null;
		for (LivingEntity ent : inSight)
			if (toThrust == null || ent.getLocation().distance(p.getLocation()) < toThrust.getLocation().distance(p.getLocation()))
				toThrust = ent;
		
		if (toThrust != null && !u.getTeam().contains(toThrust)) {
			UtilEnt.damage(damage, this.getName(), toThrust, p); /* DAMAGING */
			toThrust.addPotionEffect(EFFECT);
			
			new SoundEffect(Sound.ZOMBIE_WOOD, 1.2F).play(toThrust.getLocation()); /* CUSTOM SOUND */
			this.sendUsedMessageToPlayer(p, this.getName()); /* SENDING USED MESSAGE */
			
			/* SENDING MESSAGE OF HIT IF THE ENTITY WAS A PLAYER */
			if (toThrust instanceof Player) this.sendAbilityMessage(toThrust, "&s" + p.getName() + " &rhit you with &g" + this.getName() + " " + level + "&r.");
		} else {
			this.sendAbilityMessage(p, "You missed &g" + this.getName() + "&r.");
		}
		
		this.setCooldown(14 - level);
		return true;
	}

}
