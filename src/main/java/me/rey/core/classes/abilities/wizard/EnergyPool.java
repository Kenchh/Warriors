package me.rey.core.classes.abilities.wizard;

import java.util.Arrays;

import org.bukkit.entity.Player;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.energy.EnergyHandler;
import me.rey.core.energy.IEnergyEditor;
import me.rey.core.events.customevents.EnergyUpdateEvent;
import me.rey.core.players.User;

public class EnergyPool extends Ability implements IConstant, IEnergyEditor {

	EnergyHandler energyHandler = new EnergyHandler();
	
	public EnergyPool() {
		super(105, "Energy Pool", ClassType.GOLD, AbilityType.PASSIVE_C, 1, 3, 0.00, Arrays.asList(
				"Your maximum energy capacity",
				"is now increased by <variable>27*l</variable> (+27)."
				));
		
		this.setIgnoresCooldown(true);
		this.setInLiquid(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		EnergyUpdateEvent e = (EnergyUpdateEvent) conditions[0];
		
		e.addExtraCapacity(27 * level);
		return true;
	}

}
