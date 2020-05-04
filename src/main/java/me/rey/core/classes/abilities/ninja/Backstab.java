package me.rey.core.classes.abilities.ninja;

import java.util.Arrays;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.events.customevents.DamageEvent;
import me.rey.core.players.User;

public class Backstab extends Ability implements IPlayerDamagedEntity {

	public Backstab() {
		super(1, "Backstab", ClassType.LEATHER, AbilityType.PASSIVE_B, 1, 3, 0.00, Arrays.asList(
				"Attacks from behind opponents",
				"deal <variable>1.5*l+1.5</variable> (+1.5) additional damage."
				));
		this.setIgnoresCooldown(true);
		this.setInLiquid(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		DamageEvent e = (DamageEvent) conditions[0];
		
		Player damager = e.getDamager();
		LivingEntity damagee = e.getDamagee();

		if(isBehind(damager, damagee)) {
			
			// DAMAGE
			e.addMod(1.5*level+1.5);
			
			// PLAY EFFECTS
			damagee.getWorld().playSound(damagee.getLocation(), Sound.HURT_FLESH, 1f, 2f);
			damagee.getWorld().playEffect(damagee.getLocation(), Effect.STEP_SOUND, 55);
			return true;
		}
		return false;
	}

	private boolean isBehind(final Player attacker, final LivingEntity target) {
		return attacker.getLocation().getDirection().dot(target.getLocation().getDirection()) > 0.8;
	}
}
