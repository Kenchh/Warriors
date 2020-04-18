package me.rey.core.classes.abilities.ninja;

import java.util.Arrays;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.DamageTrigger;
import me.rey.core.events.customevents.DamageEvent;
import me.rey.core.players.User;

public class Backstab extends Ability implements DamageTrigger {

	public Backstab() {
		super(4, "Backstab", ClassType.LEATHER, AbilityType.PASSIVE_B, 1, 3, 0.00, Arrays.asList(
				"Attacks from behind opponents",
				"deal <variable>1.5*l+1.5</variable> (+1.5) additional damage."
				));
		this.setIgnoresCooldown(true);
	}

	@Override
	public void execute(User u, Player p, int level) {
		// ignore
	}

	@Override
	public boolean damageTrigger(DamageEvent e, int level) {
		Player damager = e.getDamager();
		LivingEntity damagee = e.getDamagee();
		
		Vector look = damagee.getLocation().getDirection();
		look.setY(0);
		look.normalize();
		
		Vector from = damager.getLocation().toVector().subtract(damagee.getLocation().toVector());
		from.setY(0);
		look.normalize();
		
		Vector check = new Vector(look.getX() * -1, 0, look.getZ() * -1);
		double checkDec = check.subtract(from).length();
		if(checkDec < 0.8) {
			
			// DAMAGE
			e.addMod(1.5*level+1.5);
			
			// PLAY EFFECTS
			damagee.getWorld().playSound(damagee.getLocation(), Sound.HURT_FLESH, 1f, 2f);
			damagee.getWorld().playEffect(damagee.getLocation(), Effect.STEP_SOUND, 55);
			return true;
		}
		return false;
	}

}
