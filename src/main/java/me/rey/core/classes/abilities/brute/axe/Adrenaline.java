package me.rey.core.classes.abilities.brute.axe;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.Effect;
import me.rey.core.effects.repo.Silence;
import me.rey.core.players.User;

public class Adrenaline extends Ability {

	public Adrenaline() {
		super(611, "Adrenaline", ClassType.DIAMOND, AbilityType.AXE, 1, 5, 10.0D, Arrays.asList(
				"Clear all your status effects and",
				"gain Resistance 1 and Speed 1 for",
				"<variable>5+(0.5*l)</variable> Seconds."
				));
		
		this.setWhileSilenced(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		PotionEffect RES = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) Math.round((5 + (0.5*level)) * 20), 0, false, false),
				 	 SPEED = new PotionEffect(PotionEffectType.SPEED, (int) Math.round((5 + (0.5*level)) * 20), 0, false, false);
		
		RES.apply(p	);
		SPEED.apply(p);
		
		Effect.clearAllEffects(p, Arrays.asList(Silence.class));
		return true;
	}

}
