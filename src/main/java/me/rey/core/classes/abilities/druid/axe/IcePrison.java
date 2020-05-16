package me.rey.core.classes.abilities.druid.axe;

import java.util.Arrays;

import org.bukkit.entity.Player;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;

public class IcePrison extends Ability {

	public IcePrison() {
		super(213, "Ice Prison", ClassType.GOLD, AbilityType.AXE, 1, 5, 20.00, Arrays.asList(
				""));
		
		this.setEnergyCost(57);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		
		return true;
	}
	
}
