package me.rey.core.classes.abilities.knight.passive_b;

import java.util.Arrays;

import org.bukkit.entity.Player;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.effects.repo.Bleed;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;

public class FatalBlow extends Ability implements IPlayerDamagedEntity {

	public FatalBlow() {
		super(342, "Fatal Blow", ClassType.IRON, AbilityType.PASSIVE_B, 1, 3, 0.00, Arrays.asList(
				"Hitting an enemy with your sword who",
				"has half of your health or less causes",
				"them to not be able to heal for <variable>2+l</variable> Seconds."
				));
		this.setIgnoresCooldown(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		DamageEvent e = (DamageEvent) conditions[0];
		
		this.setMessage(null);
		if(e.getDamagee().getHealth() <= p.getHealth() / 2) {
			new Bleed(this.getName(), p).apply(e.getDamagee(), 2 + level);
			return true;
		}
		
		return false;
	}

}
